package com.adobe.ids.dim.security.metrics;

public class OAuthMetricsValidator implements OAuthMetricsValidatorMBean {

    private static OAuthMetricsValidator oAuthMetrics;
    private Integer countOfRequestFailedWithoutACL;

    public OAuthMetricsValidator(){
        this.countOfRequestFailedWithoutACL = 0;
    }

    @Override
    public Integer getCountOfRequestFailedWithoutACL() {
        return countOfRequestFailedWithoutACL;
    }

    public void incCountOfRequestsFailedWithoutACL(){
        this.countOfRequestFailedWithoutACL++;
    }

    public static OAuthMetricsValidator getInstance(){
        if(oAuthMetrics == null){
            oAuthMetrics = new OAuthMetricsValidator();
        }
        return oAuthMetrics;
    }

}
