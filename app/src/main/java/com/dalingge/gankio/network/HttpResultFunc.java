package com.dalingge.gankio.network;


import com.dalingge.gankio.data.model.ResultBean;

import io.reactivex.functions.Function;


/**
 * FileName: HttpResultFunc
 * description:  用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
 *                Subscriber真正需要的数据类型，也就是Data部分的数据类型
 * Author: 丁博洋
 * Date: 2016/9/1
 */

public class HttpResultFunc<T> implements Function<ResultBean<T>, T> {
    @Override
    public T apply(ResultBean<T> httpResult) {
        if (httpResult.isError()) {
            throw new RuntimeException(httpResult.getMsg());
        }
        return httpResult.getResults();
    }
}
