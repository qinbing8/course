package com.github.hcsp.course.model;

import java.util.List;

public class PageResponse<T> {
    private Integer totalPage;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> data;

    public PageResponse() {

    }

    public PageResponse(Integer totalPage, Integer pageNum, Integer pageSize, List<T> data) {
        this.totalPage = totalPage;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.data = data;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
