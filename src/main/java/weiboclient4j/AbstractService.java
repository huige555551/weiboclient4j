package weiboclient4j;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.type.TypeReference;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import weiboclient4j.model.RateLimitStatus;
import weiboclient4j.model.Url;
import weiboclient4j.model.UrlInfo;
import weiboclient4j.oauth2.SinaWeibo2AccessToken;
import weiboclient4j.params.Paging;
import weiboclient4j.params.ParameterAction;
import weiboclient4j.params.Parameters;
import static weiboclient4j.utils.JsonUtils.parseJsonObject;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Hover Ruan
 */
public class AbstractService {
    public static final String API2_URL = "https://api.weibo.com/2/";
    public static final String ACCESS_TOKEN = "access_token";

    protected WeiboClient2 client;
    private SinaWeibo2AccessToken accessToken;

    private int connectTimeoutDuration = 30;
    private TimeUnit connectTimeoutUnit = TimeUnit.SECONDS;
    private int readTimeoutDuration = 30;
    private TimeUnit readTimeoutUnit = TimeUnit.SECONDS;

    public AbstractService(WeiboClient2 client) {
        this.client = client;

        client.initService(this);
    }

    public void setAccessToken(SinaWeibo2AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public void setConnectTimeoutDuration(int connectTimeoutDuration) {
        this.connectTimeoutDuration = connectTimeoutDuration;
    }

    public void setConnectTimeoutUnit(TimeUnit connectTimeoutUnit) {
        this.connectTimeoutUnit = connectTimeoutUnit;
    }

    public void setReadTimeoutDuration(int readTimeoutDuration) {
        this.readTimeoutDuration = readTimeoutDuration;
    }

    public void setReadTimeoutUnit(TimeUnit readTimeoutUnit) {
        this.readTimeoutUnit = readTimeoutUnit;
    }

    public static Parameters withParams(ParameterAction... actions) {
        Parameters params = Parameters.create();

        for (ParameterAction action : actions) {
            if (action != null) {
                action.addParameter(params);
            }
        }

        return params;
    }

    public <T> T doGet(String path, Paging paging, Parameters params, Class<T> clazz) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), paging, params, clazz);
    }

    public <T> T doGet(String path, Parameters params, Class<T> clazz) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), params, clazz);
    }

    public <T> T doGet(String path, Paging paging, Class<T> clazz) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), paging, clazz);
    }

    public <T> T doGet(String path, Class<T> clazz) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), clazz);
    }

    public <T> List<T> doGet(String path, TypeReference<List<T>> typeReference) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), typeReference);
    }

    public <T> List<T> doGet(String path, Paging paging, TypeReference<List<T>> typeReference) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), paging, typeReference);
    }

    public <T> List<T> doGet(String path, Parameters params, TypeReference<List<T>> typeReference)
            throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), params, typeReference);
    }

    public <T> List<T> doGet(String path, Paging paging, Parameters params,
                             TypeReference<List<T>> typeReference) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createGetRequest(path), paging, params, typeReference);
    }

    public <T> T doPost(String path, Paging paging, Parameters params, Class<T> clazz) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createPostRequest(path), paging, params, clazz);
    }

    public <T> T doPost(String path, Parameters params, Class<T> clazz) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createPostRequest(path), params, clazz);
    }

    public <T> List<T> doPost(String path, Parameters params, TypeReference<List<T>> typeReference) throws WeiboClientException {
        return sendRequestAndGetResponseObject(createPostRequest(path), params, typeReference);
    }

    public <T> List<T> sendRequestAndGetResponseObject(OAuthRequest request, Parameters params,
                                                       TypeReference<List<T>> typeReference) throws WeiboClientException {
        return sendRequestAndGetResponseObject(request, Paging.EMPTY, params, typeReference);
    }

    public <T> List<T> sendRequestAndGetResponseObject(OAuthRequest request, Paging paging,
                                                       TypeReference<List<T>> typeReference) throws WeiboClientException {
        return sendRequestAndGetResponseObject(request, paging, Parameters.create(), typeReference);
    }

    public <T> List<T> sendRequestAndGetResponseObject(OAuthRequest request, Paging paging, Parameters params,
                                                       TypeReference<List<T>> typeReference) throws WeiboClientException {
        if (paging != null) {
            params.add(paging);
        }

        params.appendTo(request);

        return sendRequestAndGetResponseObject(request, typeReference);
    }

    public <T> List<T> sendRequestAndGetResponseObject(OAuthRequest request, TypeReference<List<T>> typeReference)
            throws WeiboClientException {
        Response response = request.send();

        return parseJsonObject(response, typeReference);
    }

    public <T> T sendRequestAndGetResponseObject(OAuthRequest request, Parameters params, Class<T> clazz)
            throws WeiboClientException {
        return sendRequestAndGetResponseObject(request, Paging.EMPTY, params, clazz);
    }

    public <T> T sendRequestAndGetResponseObject(OAuthRequest request, Paging paging, Class<T> clazz)
            throws WeiboClientException {
        return sendRequestAndGetResponseObject(request, paging, Parameters.create(), clazz);
    }

    public <T> T sendRequestAndGetResponseObject(OAuthRequest request, Paging paging, Parameters params, Class<T> clazz)
            throws WeiboClientException {
        if (paging != null) {
            params.add(paging);
        }

        params.appendTo(request);

        return sendRequestAndGetResponseObject(request, clazz);
    }

    public <T> T sendRequestAndGetResponseObject(OAuthRequest request, Class<T> clazz) throws WeiboClientException {
        Response response = request.send();

        return parseJsonObject(response, clazz);
    }

    public OAuthRequest createGetRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, getFullPath(path));
        setRequestTimeout(request);

        if (accessToken != null) {
            request.addQuerystringParameter(ACCESS_TOKEN, accessToken.getToken());
        }

        return request;
    }

    public OAuthRequest createPostRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.POST, getFullPath(path));
        setRequestTimeout(request);

        if (accessToken != null) {
            request.addBodyParameter(ACCESS_TOKEN, accessToken.getToken());
        }

        return request;
    }

    private void setRequestTimeout(OAuthRequest request) {
        request.setConnectTimeout(connectTimeoutDuration, connectTimeoutUnit);
        request.setReadTimeout(readTimeoutDuration, readTimeoutUnit);
    }

    public String getFullPath(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        } else {
            return API2_URL + path + ".json";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class TagActionResponse {
        private long tagid;

        public long getTagid() {
            return tagid;
        }

        public void setTagid(long tagid) {
            this.tagid = tagid;
        }

        public static List<Long> toLongList(List<TagActionResponse> responseList) {
            List<Long> result = new ArrayList<Long>(responseList.size());
            for (TagActionResponse response : responseList) {
                result.add(response.getTagid());
            }

            return result;
        }
    }

    protected static final TypeReference<List<TagActionResponse>> TYPE_TAG_ACTION_RESPONSE_LIST =
            new TypeReference<List<TagActionResponse>>() {
            };

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class LongIdStringValue {
        private long id;
        private String value;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    protected static final TypeReference<List<LongIdStringValue>> TYPE_LONG_ID_STRING_VALUE_LIST =
            new TypeReference<List<LongIdStringValue>>() {
            };

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class FollowTrendResponse {
        private long topicid;

        public long getTopicid() {
            return topicid;
        }

        public void setTopicid(long topicid) {
            this.topicid = topicid;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class UrlInfoResponse {
        private List<UrlInfo> urls;

        public List<UrlInfo> getUrls() {
            return urls;
        }

        public void setUrls(List<UrlInfo> urls) {
            this.urls = urls;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class UrlResponse {
        private List<Url> urls;

        public List<Url> getUrls() {
            return urls;
        }

        public void setUrls(List<Url> urls) {
            this.urls = urls;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class ResultResponse {
        private boolean result;

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }
    }

    protected static SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class RawRateLimitStatus {
        private int ipLimit;
        private String limitTimeUnit;
        private int remainingIpHits;
        private int remainingUserHits;
        private String resetTime;
        private int resetTimeInSeconds;
        private int userLimit;

        public RateLimitStatus asRateLimitStatus() throws ParseException {
            RateLimitStatus status = new RateLimitStatus();
            status.setIpLimit(ipLimit);
            status.setLimitTimeUnit(limitTimeUnit);
            status.setRemainingUserHits(remainingUserHits);
            status.setResetTimeInSeconds(resetTimeInSeconds);
            status.setUserLimit(userLimit);

            status.setResetTime(simpleFormat.parse(resetTime));

            return status;
        }

        public int getIpLimit() {
            return ipLimit;
        }

        public void setIpLimit(int ipLimit) {
            this.ipLimit = ipLimit;
        }

        public String getLimitTimeUnit() {
            return limitTimeUnit;
        }

        public void setLimitTimeUnit(String limitTimeUnit) {
            this.limitTimeUnit = limitTimeUnit;
        }

        public int getRemainingIpHits() {
            return remainingIpHits;
        }

        public void setRemainingIpHits(int remainingIpHits) {
            this.remainingIpHits = remainingIpHits;
        }

        public int getRemainingUserHits() {
            return remainingUserHits;
        }

        public void setRemainingUserHits(int remainingUserHits) {
            this.remainingUserHits = remainingUserHits;
        }

        public String getResetTime() {
            return resetTime;
        }

        public void setResetTime(String resetTime) {
            this.resetTime = resetTime;
        }

        public int getResetTimeInSeconds() {
            return resetTimeInSeconds;
        }

        public void setResetTimeInSeconds(int resetTimeInSeconds) {
            this.resetTimeInSeconds = resetTimeInSeconds;
        }

        public int getUserLimit() {
            return userLimit;
        }

        public void setUserLimit(int userLimit) {
            this.userLimit = userLimit;
        }
    }

    public static final TypeReference<List<Map<String, String>>> LIST_MAP_S_S_TYPE_REFERENCE = new TypeReference<List<Map<String, String>>>() {
    };

    protected Map<String, String> mergeSingleItemMap(List<Map<String, String>> response) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map<String, String> item : response) {
            map.putAll(item);
        }

        return map;
    }

    protected ParameterAction urlParam(final URL url) {
        return new ParameterAction() {
            public void addParameter(Parameters params) {
                if (url != null) {
                    params.add("url", url.toExternalForm());
                }
            }
        };
    }
}