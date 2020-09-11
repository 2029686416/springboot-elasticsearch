package com.demon.config;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.SearchHit;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.ScriptedField;
import org.springframework.data.elasticsearch.core.AbstractResultMapper;
import org.springframework.data.elasticsearch.core.DefaultEntityMapper;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 包冲突，将方法提取出来
 */
@Component
public class EsResultMapping extends AbstractResultMapper {//DefaultResultMapper

    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
    private final ConversionService conversionService = new DefaultConversionService();

    public EsResultMapping() {
        this(new SimpleElasticsearchMappingContext());
    }

    public EsResultMapping(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        this(mappingContext, initEntityMapper(mappingContext));
    }

    public EsResultMapping(EntityMapper entityMapper) {
        this(new SimpleElasticsearchMappingContext(), entityMapper);
    }

    public EsResultMapping(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext,
            @Nullable EntityMapper entityMapper) {

        super(entityMapper != null ? entityMapper : initEntityMapper(mappingContext));
        this.mappingContext = mappingContext;
    }

    private static EntityMapper initEntityMapper(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {

        Assert.notNull(mappingContext, "MappingContext must not be null!");
        return new DefaultEntityMapper(mappingContext);
    }

    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {

        Long totalHits = response.getHits().getTotalHits().value;
        float maxScore = response.getHits().getMaxScore();

        List<T> results = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            if (hit != null) {
                T result = null;
                String hitSourceAsString = hit.getSourceAsString();
                if (!StringUtils.isEmpty(hitSourceAsString)) {
                    result = mapEntity(hitSourceAsString, clazz);
                } else {
                    result = mapEntity(hit.getFields().values(), clazz);
                }

                setPersistentEntityId(result, hit.getId(), clazz);
                setPersistentEntityVersion(result, hit.getVersion(), clazz);
                setPersistentEntityScore(result, hit.getScore(), clazz);

                populateScriptFields(result, hit);
                results.add(result);
            }
        }

        return new AggregatedPageImpl<T>(results, pageable, totalHits, response.getAggregations(), response.getScrollId(),
                maxScore);
    }

    private <T> void populateScriptFields(T result, SearchHit hit) {
        if (hit.getFields() != null && !hit.getFields().isEmpty() && result != null) {
            for (java.lang.reflect.Field field : result.getClass().getDeclaredFields()) {
                ScriptedField scriptedField = field.getAnnotation(ScriptedField.class);
                if (scriptedField != null) {
                    String name = scriptedField.name().isEmpty() ? field.getName() : scriptedField.name();
                    DocumentField searchHitField = hit.getFields().get(name);
                    if (searchHitField != null) {
                        field.setAccessible(true);
                        try {
                            field.set(result, searchHitField.getValue());
                        } catch (IllegalArgumentException e) {
                            throw new ElasticsearchException(
                                    "failed to set scripted field: " + name + " with value: " + searchHitField.getValue(), e);
                        } catch (IllegalAccessException e) {
                            throw new ElasticsearchException("failed to access scripted field: " + name, e);
                        }
                    }
                }
            }
        }
    }

    private <T> T mapEntity(Collection<DocumentField> values, Class<T> clazz) {
        return mapEntity(buildJSONFromFields(values), clazz);
    }

    private String buildJSONFromFields(Collection<DocumentField> values) {
        JsonFactory nodeFactory = new JsonFactory();
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
            generator.writeStartObject();
            for (DocumentField value : values) {
                if (value.getValues().size() > 1) {
                    generator.writeArrayFieldStart(value.getName());
                    for (Object val : value.getValues()) {
                        generator.writeObject(val);
                    }
                    generator.writeEndArray();
                } else {
                    generator.writeObjectField(value.getName(), value.getValue());
                }
            }
            generator.writeEndObject();
            generator.flush();
            return new String(stream.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public <T> T mapResult(GetResponse response, Class<T> clazz) {
        T result = mapEntity(response.getSourceAsString(), clazz);
        if (result != null) {
            setPersistentEntityId(result, response.getId(), clazz);
            setPersistentEntityVersion(result, response.getVersion(), clazz);
        }
        return result;
    }

    @Override
    public <T> List<T> mapResults(MultiGetResponse responses, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (MultiGetItemResponse response : responses.getResponses()) {
            if (!response.isFailed() && response.getResponse().isExists()) {
                T result = mapEntity(response.getResponse().getSourceAsString(), clazz);
                setPersistentEntityId(result, response.getResponse().getId(), clazz);
                setPersistentEntityVersion(result, response.getResponse().getVersion(), clazz);
                list.add(result);
            }
        }
        return list;
    }

    private <T> void setPersistentEntityId(T result, String id, Class<T> clazz) {

        if (clazz.isAnnotationPresent(Document.class)) {

            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(clazz);
            ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();



//			PersistentPropertyAccessor<T> accessor = new ConvertingPropertyAccessor<>(
//					persistentEntity.getPropertyAccessor(result), conversionService);

            // Only deal with String because ES generated Ids are strings !
            if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
                persistentEntity.getPropertyAccessor(result).setProperty(idProperty, id);
            }
        }
    }

    private <T> void setPersistentEntityVersion(T result, long version, Class<T> clazz) {

        if (clazz.isAnnotationPresent(Document.class)) {

            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(clazz);
            ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();

            // Only deal with Long because ES versions are longs !
            if (versionProperty != null && versionProperty.getType().isAssignableFrom(Long.class)) {
                // check that a version was actually returned in the response, -1 would indicate that
                // a search didn't request the version ids in the response, which would be an issue
                Assert.isTrue(version != -1, "Version in response is -1");
                persistentEntity.getPropertyAccessor(result).setProperty(versionProperty, version);
            }
        }
    }

    private <T> void setPersistentEntityScore(T result, float score, Class<T> clazz) {

        if (clazz.isAnnotationPresent(Document.class)) {

            ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);

            if (!entity.hasScoreProperty()) {
                return;
            }

            entity.getPropertyAccessor(result) //
                    .setProperty(entity.getScoreProperty(), score);
        }
    }

//	private MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
//
//    public EsResultMapping() {
//        super(new DefaultEntityMapper());
//    }
//
//    public EsResultMapping(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
//        super(new DefaultEntityMapper());
//        this.mappingContext = mappingContext;
//    }
//
//    public EsResultMapping(EntityMapper entityMapper) {
//        super(entityMapper);
//    }
//
//    public EsResultMapping(
//            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext,
//            EntityMapper entityMapper) {
//        super(entityMapper);
//        this.mappingContext = mappingContext;
//    }
//
//    @Override
//    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
//        long totalHits = response.getHits().getTotalHits().value;
//        List<T> results = new ArrayList<>();
//        for (SearchHit hit : response.getHits()) {
//            if (hit != null) {
//                T result = null;
//                if (StringUtils.hasText(hit.getSourceAsString())) {
//                    result = mapEntity(hit.getSourceAsString(), clazz);
//                } else {
//                    //result = mapEntity(hit.getFields().values(), clazz);
//                }
//                setPersistentEntityId(result, hit.getId(), clazz);
//                setPersistentEntityVersion(result, hit.getVersion(), clazz);
//                populateScriptFields(result, hit);
//
//               // 高亮查询
//               // populateHighLightedFields(result, hit.getHighlightFields());
//                results.add(result);
//            }
//        }
//
//        return new AggregatedPageImpl<T>(results, pageable, totalHits, response.getAggregations(), response.getScrollId());
//    }
//
//    private <T>  void populateHighLightedFields(T result, Map<String, HighlightField> highlightFields) {
//        for (HighlightField field : highlightFields.values()) {
//            try {
//                PropertyUtils.setProperty(result, field.getName(), concat(field.fragments()));
//            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
//                throw new ElasticsearchException("failed to set highlighted value for field: " + field.getName()
//                        + " with value: " + Arrays.toString(field.getFragments()), e);
//            }
//        }
//    }
//
//    private String concat(Text[] texts) {
//        StringBuffer sb = new StringBuffer();
//        for (Text text : texts) {
//            sb.append(text.toString());
//        }
//        return sb.toString();
//    }
//
//    private <T> void populateScriptFields(T result, SearchHit hit) {
//        if (hit.getFields() != null && !hit.getFields().isEmpty() && result != null) {
//            for (java.lang.reflect.Field field : result.getClass().getDeclaredFields()) {
//                ScriptedField scriptedField = field.getAnnotation(ScriptedField.class);
//                if (scriptedField != null) {
//                    String name = scriptedField.name().isEmpty() ? field.getName() : scriptedField.name();
//                    SearchHitField searchHitField = hit.getFields().get(name);
//                    if (searchHitField != null) {
//                        field.setAccessible(true);
//                        try {
//                            field.set(result, searchHitField.getValue());
//                        } catch (IllegalArgumentException e) {
//                            throw new ElasticsearchException("failed to set scripted field: " + name + " with value: "
//                                    + searchHitField.getValue(), e);
//                        } catch (IllegalAccessException e) {
//                            throw new ElasticsearchException("failed to access scripted field: " + name, e);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private <T> T mapEntity(Collection<SearchHitField> values, Class<T> clazz) {
//        return mapEntity(buildJSONFromFields(values), clazz);
//    }
//
//    private String buildJSONFromFields(Collection<SearchHitField> values) {
//        JsonFactory nodeFactory = new JsonFactory();
//        try {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
//            generator.writeStartObject();
//            for (SearchHitField value : values) {
//                if (value.getValues().size() > 1) {
//                    generator.writeArrayFieldStart(value.getName());
//                    for (Object val : value.getValues()) {
//                        generator.writeObject(val);
//                    }
//                    generator.writeEndArray();
//                } else {
//                    generator.writeObjectField(value.getName(), value.getValue());
//                }
//            }
//            generator.writeEndObject();
//            generator.flush();
//            return new String(stream.toByteArray(), Charset.forName("UTF-8"));
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    @Override
//    public <T> T mapResult(GetResponse response, Class<T> clazz) {
//        T result = mapEntity(response.getSourceAsString(), clazz);
//        if (result != null) {
//            setPersistentEntityId(result, response.getId(), clazz);
//            setPersistentEntityVersion(result, response.getVersion(), clazz);
//        }
//        return result;
//    }
//
//    @Override
//    public <T> LinkedList<T> mapResults(MultiGetResponse responses, Class<T> clazz) {
//        LinkedList<T> list = new LinkedList<>();
//        for (MultiGetItemResponse response : responses.getResponses()) {
//            if (!response.isFailed() && response.getResponse().isExists()) {
//                T result = mapEntity(response.getResponse().getSourceAsString(), clazz);
//                setPersistentEntityId(result, response.getResponse().getId(), clazz);
//                setPersistentEntityVersion(result, response.getResponse().getVersion(), clazz);
//                list.add(result);
//            }
//        }
//        return list;
//    }
//
//    private <T> void setPersistentEntityId(T result, String id, Class<T> clazz) {
//
//        if (mappingContext != null && clazz.isAnnotationPresent(Document.class)) {
//
//            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(clazz);
//            ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();
//
//            // Only deal with String because ES generated Ids are strings !
//            if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
//                persistentEntity.getPropertyAccessor(result).setProperty(idProperty, id);
//            }
//
//        }
//    }
//
//    private <T> void setPersistentEntityVersion(T result, long version, Class<T> clazz) {
//        if (mappingContext != null && clazz.isAnnotationPresent(Document.class)) {
//
//            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(clazz);
//            ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();
//
//            // Only deal with Long because ES versions are longs !
//            if (versionProperty != null && versionProperty.getType().isAssignableFrom(Long.class)) {
//                // check that a version was actually returned in the response, -1 would indicate that
//                // a search didn't request the version ids in the response, which would be an issue
//                Assert.isTrue(version != -1, "Version in response is -1");
//                persistentEntity.getPropertyAccessor(result).setProperty(versionProperty, version);
//            }
//        }
//    }


}

