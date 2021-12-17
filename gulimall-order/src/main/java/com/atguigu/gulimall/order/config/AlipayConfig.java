package com.atguigu.gulimall.order.config;

public class AlipayConfig {

    /**
     * 应用ID
     */
    public static String APP_ID = "2021000116664029";

    /**
     * 商户私钥
     */
    public static String MERCHANT_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCS04WopKAhPYbX52sFbs3mPGaYP5l/QWGH71tQOM5vpdcD6d4zDwoN6g4HU+DnZE420jlNiHaWkfssd6LQj6I89z5rwMF1eFf279UHmFugIyIXU6se5scbncV9HwnLiobq1ISa2z3xdB6CH325dHlFXBeMa6jw5lEo8jFiJL2jOzxjkNj7/c9+m5jSXi9tXc0FH/UFuz5xkIWmXNc33P5PE97Xr5Lke+3Lz5VmBXINUaw7peW5oTjWdf/hL5o376/mJBL53kTUDGIp6qE3cf3bj07GSgi8/nJaEJSpRHhwiMEsxLu0F54WUDCDiKRgQboiP0iQnQkbuLkMXMdtEQyJAgMBAAECggEAaSGH3L8z4Vk6yjFr0hIK4EZzd6RidloMSdc7XO+v8QWgNa8paQnluxzSOrD2oviRgHN6kGeb8ihI3wgyzn9qPZkTWvhEDabD5Y7IzI1tXfaKHIXOS2wXOVD47aRh0Ro5jL/qyy8c5Vyt06JU1YvuoZlVSzhzWTqw9k6kyY9hMhTMM2QULniN1toy1rdywP9qGQew1FA+yDfCG0uT0eA1nVR6lOLKZVE00B3DMWtDYVaZ6YFyXxR4PdwW9bWZF6YneVBIrZV4ofQ7bl6BhhIy0VrHlGvPYGNYo5eOeAZKjmkGHVekm/ZYz3bZUj4X9rAO3aj+piuXGewf/Kpv9LTdOQKBgQDK/NBZI8gC73E6aB2sNPQiTB1e2yZJPgv14ikXy8nRH7Izy6MEpoqXxZno3gRlWbTlNLXpW+fM2LY1WKf3MNtZcloiWw7u7AAxqm1HjOgPXyPVzRF4NeJziVK3bayTUrPVJpGtdCoZ8lzE+4nwrry+pXv4ZpBzGjlT5nu2w5rPEwKBgQC5K+teyCgHJ0ias/8a8YY/SBs5U4vJaee6YNQN0bg/q6ThDxi5n2k5IBCrFagOitEqDYq+xZRfWs44vo6avVOacLwxPuc31uQg3AvYiKP1u7RiYbH1jLxCKaF+7BJBouFYtGRAsUZbyxhe4TGh5Mc8M2kRebJYxCaEKb1AoWe9cwKBgDyup3p+f+SgwDC9v2Ej03gNWRpqWV+79Mdk/PPoMEUcPKHPVsSlJPmKUSCUfqvtzW5X+6nvl7RddNstzcjQCkMLNHbg0e4LD2PJlzG0pgsyXfwkefx931y6yMedOeIw7yHhjdtUxuFK2wDoUaxqEHLN9emkf7ppNIoodQOGJlmRAoGANz78RVYQYzn8NnPXe9FtjTnVLYBYp2oaggfbnJfikdSDJ8aaSN9fxHRlpLWcY2JVT2ek6pAGWYC9ID7ySwaLh/t0mni7AXTysT8Vpa/2wx0Sj3u4XZt0m4TSMwz50W1oME/5zTdW15I7nisv5K5eJv7rrRQS+tri5vGRDlRZoDMCgYA/1E7qfYnOa6Kqyv0bYU2f/2do5leSZos96GWOzsiGEn2S1T22S8Tf+9ozTZQxB8JfliDAslSYz9PwYqrKqmEhWDxRDlhN5S9u8mx1uAJU9AD+BIiSHM1HbLr4GDscMXvRWMFTA89mofqP8LRXfICZVbZ51QK/dvjGvYsRIguO1Q==";

    /**
     * 支付宝公钥
     */
    public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA86r8MtgqoGBufQMAgmmeCybnZwbqnc0ZoSpGcdsfkCQ0NKb2kxNja7QKkSVUdqnfhGOlbl1mar6w5LyUO9dwGwfCd5JuGlKCE4BuxYra8tY8hP9U62iDIfPeDfvVHHzmKdb0BVrqDXog8yt/RDYP4ZZkIHEeQV60N19DUqo5JWkarIinmCOU4ki9mugd0n+OshlSC05SiO+q7XBLRx3ogP+fsTcBPvlIkV07cqJPUOxWb+sx7oa+JWj07jFUAxZQDyHzI07JPHRp5eBgsCNAO60LI7Iu6kOhdQl1JjUqNYKfr3RZ+YlfyxPOy8pFuzIBtyBqM51Xzo/ZB1QYnjydbQIDAQAB";

    /**
     * 支付宝网关
     */
    public static String GATEWAY_URL = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 服务器异步通知页面路径
     * 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     */
    public static String NOTIFY_URL = "http://www.baidu.com";

    /**
     * 页面跳转同步通知页面路径
     * http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问需
     */
    public static String RETURN_URL = "http://www.baidu.com";

    /**
     * 编码
     */
    public static String CHARSET = "UTF-8";

    /**
     * 返回格式
     */
    public static String FORMAT = "json";

    /**
     * 加密方式
     */
    public static String SIGN_TYPE = "RSA2";

    /**
     * 超时时间
     */
    public static String TIMEOUT_EXPRESS = "30m";



}