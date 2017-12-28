//package com.creatilas.brightstest;
//
///**
// * Created by rusci on 28-Dec-17.
// */
//
//public class RetrofitBaseClient {
//    public static Retrofit create() {
//        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//        httpClient.addInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Interceptor.Chain chain) throws IOException {
//                Request original = chain.request();
//
//                Request request = original.newBuilder()
//                        .header("Authorization", "0DVbinOxY0K4pjB0rrbevjjMBuvPipUKTtY92yBRKPI=")
//                        .method(original.method(), original.body())
//                        .build();
//
//                return chain.proceed(request);
//            }
//        });
//        OkHttpClient client = httpClient.build();
//        return new Retrofit.Builder()
//                .baseUrl("https://creatilas.com/api/footballbetcoach/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
//                .build();
//    }
//}
