package lyh.seaweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liuwei on 17-7-23.
 */

public class Now {
    @SerializedName("tmp")//让JSON字段和Java字段建立映射
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
