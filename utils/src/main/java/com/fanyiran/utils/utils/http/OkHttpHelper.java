package com.fanyiran.utils.utils.http;

import com.fanyiran.utils.CollectionUtils;
import com.fanyiran.utils.LogUtil;
import com.fanyiran.utils.utils.http.callback.OnGetFileCallback;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

public class OkHttpHelper implements IHttpHelper {
    private OkHttpClient okHttpClient;

    public OkHttpHelper() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        okHttpClient = builder
//                .addNetworkInterceptor(logInterceptor)
                .build();
    }

    @Override
    public <T extends JsonRequestResult> JsonResult post(String url, Map<String, String> formBodyMap, Class<T> jsonClass) {
        return post(url, null, formBodyMap, null, jsonClass);
    }

    @Override
    public <T extends JsonRequestResult> JsonResult post(String url, String picPath, Map<String, String> formBodyMap, Class<T> jsonClass) {
        return post(url, picPath, formBodyMap, null, jsonClass);
    }

    @Override
    public <T extends JsonRequestResult> JsonResult post(String url, Map<String, String> formBodyMap, Map<String, String> headerMap, Class<T> jsonClass) {
        return post(url, null, formBodyMap, headerMap, jsonClass);
    }

    @Override
    public <T extends JsonRequestResult> JsonResult post(String url, String imgPath, Map<String, String> formBodyMap, Map<String, String> headerMap, Class<T> jsonClass) {
        Request request = getRequest(url, imgPath, formBodyMap, headerMap);
        final Response[] response = new Response[1];
        String responseBody;
        JsonResult jsonResult = new JsonResult(ERROR_IO);
        try {
            response[0] = okHttpClient.newCall(request).execute();
            responseBody = response[0].body().string();
        } catch (UnknownHostException exception) {
            exception.printStackTrace();
            return jsonResult;
        } catch (IOException e) {
            e.printStackTrace();
            return jsonResult;
        }
        try {
            LogUtil.v("requestResult:", responseBody);
            T fromJson = new Gson().fromJson(responseBody, jsonClass);
            jsonResult.set(fromJson);
            jsonResult.setHttpErrorCode(SUCCESS);
            LogUtil.v("requestResult json:", fromJson.toString());
            return jsonResult;
        } catch (JsonSyntaxException e) {
        }
        return new JsonResult(ERROR_JSON);
    }

    @Override
    public void getFile(String fileUrl, String saveFileUrl, OnGetFileCallback onGetFileListener) {
        getFileInner(fileUrl, saveFileUrl, onGetFileListener);
    }

    public void getFileInner(String url, final String savePath, final OnGetFileCallback listener) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // TODO: 2019-08-12 失败处理
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File file = new File(savePath);
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    if (listener != null)
                        listener.onGetFile(savePath);
                } catch (Exception e) {
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    private Request getRequest(String url, String imgPath, Map<String, String> formBodyMap, Map<String, String> headerMap) {
        if (imgPath != null) {
            return getImageRequest(url, imgPath, formBodyMap, headerMap);
        } else {
            return getTextRequest(url, formBodyMap, headerMap);
        }
    }

    private Request getImageRequest(String url, String imgPath, Map<String, String> formBodyMap, Map<String, String> headerMap) {
        File file = new File(imgPath);
        Request.Builder requestBuilder = new Request.Builder();
//        RequestBody image = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody image = RequestBody.create(MediaType.parse("audio/basic"), file);
        MultipartBody.Builder builder = new MultipartBody.Builder();
//        if (!CollectionUtils.isEmpty(formBodyMap)) {
//            for (String key : formBodyMap.keySet()) {
//                builder.addFormDataPart(key, formBodyMap.get(key));
//            }
//        }
        builder.setType(MultipartBody.FORM)
                .addFormDataPart("fileUpload", imgPath, image);
        if (!CollectionUtils.isEmpty(headerMap)) {
            for (String key : headerMap.keySet()) {
                requestBuilder.header(key, headerMap.get(key));
            }
        }
        return requestBuilder
                .url(url)
                .post(builder.build())
                .build();
    }

    private Request getTextRequest(String url, Map<String, String> formBodyMap, Map<String, String> headerMap) {
        FormBody.Builder builder = new FormBody.Builder();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        if (!CollectionUtils.isEmpty(formBodyMap)) {
            for (String key : formBodyMap.keySet()) {
                builder.add(key, formBodyMap.get(key));
            }
            requestBuilder.post(builder.build());

        } else {
            requestBuilder.post(Util.EMPTY_REQUEST);
        }
        if (!CollectionUtils.isEmpty(headerMap)) {
            for (String key : headerMap.keySet()) {
                requestBuilder.header(key, headerMap.get(key));
            }
        }
        return requestBuilder.build();
    }
}
