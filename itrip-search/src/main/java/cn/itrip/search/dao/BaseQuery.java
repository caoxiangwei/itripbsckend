package cn.itrip.search.dao;

import cn.itrip.common.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class BaseQuery {
    private HttpSolrClient client;
    String url = PropertiesUtils.get("database.properties","baseurl");
    public BaseQuery(){
        client = new HttpSolrClient.Builder(url).withConnectionTimeout(100000).withSocketTimeout(600000).build();
    }

    public Page queryPage(SolrQuery query,Integer pageNo,Integer pageSize,Class clz) throws IOException, SolrServerException {
        // 每页显示几条，当pageSize(前端传入的参数)为空，默认值为每页10条，
        // 当pageSize(前端传入的参数)为非空，每页显示条数为pageSize(前端传入的参数)
        int rows = EmptyUtils.isEmpty(pageSize) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        // 当前页码  当pageNo(前端传入的参数)为空，当前页码默认值为1，
        // 当pageNo(前端传入的参数)不为空，当前页码为pageNo(前端传入的参数);
        int cuurentPage = EmptyUtils.isEmpty(pageNo) ? Constants.DEFAULT_PAGE_NO : pageNo;
        //从第几条查  当前页码-1乘条数
        int start = (cuurentPage - 1) * rows;
        // 拼接分页查询的条件，每页显示多少条数据
        query.setRows(rows);
        // 从第几条开始
        query.setStart(start);
        //solr查询
        QueryResponse response = client.query(query);
        //获取数据
        List list = response.getBeans(clz);
        //获取总条数
        SolrDocumentList results = response.getResults();
        long total = results.getNumFound();
        //组装page
        Page page = new Page(cuurentPage,rows,(int) total);
        page.setRows(list);
        return page;
    }

    public List queryList(SolrQuery query,Integer pageSize,Class clz) throws IOException, SolrServerException {
        // 每页显示几条，当pageSize(前端传入的参数)为空，默认值为每页10条，
        // 当pageSize(前端传入的参数)不为空，每页显示条数为pageSize(前端传入的参数)
        int rows = EmptyUtils.isEmpty(pageSize) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        // 从第一条显示
        query.setStart(0);
        // 每页显示多少条数据，前端传入的多少显示多少条，前端没传入就用默认值10条
        query.setRows(rows);
        QueryResponse response = client.query(query);
        List list = response.getBeans(clz);
        return list;
    }
}
