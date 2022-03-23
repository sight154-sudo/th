package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.UserLocation;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.dubbo.server.vo.UserLocationVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.bucket.range.GeoDistanceAggregationBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 10:20 2021/8/15
 * @description:
 */
@Service(version = "1.0.0")
@Slf4j
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    //初始化
    @PostConstruct
    public void init(){
        //判断索引是否存在
        if(!elasticsearchTemplate.indexExists("tanhua")){
            elasticsearchTemplate.createIndex(UserLocation.class);
        }
        //判断映射是否存在
        if(!elasticsearchTemplate.typeExists("tanhua","user_location")){
            //不存在，则创建
            elasticsearchTemplate.putMapping(UserLocation.class);
        }
    }

    /**
     * 更新用户的地理位置
     * @param userId
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    @Override
    public Boolean updateUserLocation(Long userId, Double longitude, Double latitude, String address) {
        //更新用户地理位置
        //若用户地理位置不存在 则添加
        GetQuery getQuery = new GetQuery();
        getQuery.setId(String.valueOf(userId));
        UserLocation userLocation = elasticsearchTemplate.queryForObject(getQuery, UserLocation.class);
        try {
            if(ObjectUtil.isEmpty(userLocation)){
                //用户地理位置不存在 则添加
                userLocation = new UserLocation();
                userLocation.setUserId(userId);
                userLocation.setLocation(new GeoPoint(latitude,longitude));
                userLocation.setCreated(System.currentTimeMillis());
                userLocation.setUpdated(userLocation.getCreated());
                userLocation.setLastUpdated(userLocation.getCreated());
                userLocation.setAddress(address);
                IndexQuery query = new IndexQueryBuilder().withObject(userLocation).build();
                elasticsearchTemplate.index(query);
            }else{
                //存在，则更新
                Map<String,Object> map = new HashMap<>();
                map.put("location",new GeoPoint(latitude,longitude));
                map.put("address",address);
                map.put("updated",System.currentTimeMillis());
                map.put("lastUpdated",userLocation.getLastUpdated());
                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.doc(map);
                UpdateQuery query = new UpdateQueryBuilder()
                        .withId(String.valueOf(userId))
                        .withClass(UserLocation.class)
                        .withUpdateRequest(updateRequest).build();

                elasticsearchTemplate.update(query);
            }
            return true;
        } catch (Exception e) {
            log.error("更新地理位置失败~ userId = " + userId + ", longitude = " + longitude +
                    ", latitude = " + latitude + ", address = " + address, e);
        }
        return false;
    }

    /**
     * 查询用户的地理位置
     * @param userId
     * @return
     */
    @Override
    public UserLocationVo queryUserLocation(Long userId) {
        GetQuery query = new GetQuery();
        query.setId(String.valueOf(userId));
        UserLocation userLocation = this.elasticsearchTemplate.queryForObject(query, UserLocation.class);
        if(ObjectUtil.isEmpty(userLocation)){
            return null;
        }
        return UserLocationVo.format(userLocation);
    }

    /**
     * 搜索附近的人
     * @param longitude
     * @param latitude
     * @param distance
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Double
            distance, Integer page, Integer pageSize) {
        PageInfo<UserLocationVo> pageInfo = new PageInfo<>();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pageSize);
        String fieldName = "location";
        //分页查询
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        PageRequest pageable = PageRequest.of(page-1,pageSize);
        searchQueryBuilder.withPageable(pageable);
        //设置中心点
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder(fieldName);
        //中心点
        geoDistanceQueryBuilder.point(new GeoPoint(latitude,longitude));
        //距离 单位: 公里
        geoDistanceQueryBuilder.distance(distance/1000, DistanceUnit.KILOMETERS);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //设置距离条件
        boolQueryBuilder.must(geoDistanceQueryBuilder);
        searchQueryBuilder.withQuery(boolQueryBuilder);

        //排序  由近到远排序
        GeoDistanceSortBuilder geoDistanceSortBuilder =
                new GeoDistanceSortBuilder(fieldName, latitude, longitude);
        geoDistanceSortBuilder.order(SortOrder.ASC);//正序
        geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS);//单位
        searchQueryBuilder.withSort(geoDistanceSortBuilder);

        //执行查询
        AggregatedPage<UserLocation> aggregatedPage =
                this.elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), UserLocation.class);
        List<UserLocation> content = aggregatedPage.getContent();
        //判断结果
        if(CollUtil.isEmpty(content)){
            return pageInfo;
        }
        pageInfo.setRecords(UserLocationVo.formatToList(content));
        return pageInfo;
    }
}
