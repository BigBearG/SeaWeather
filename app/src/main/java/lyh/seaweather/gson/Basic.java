package lyh.seaweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liuwei on 17-7-23.
 */

public class Basic {
    @SerializedName("city")//让JSON字段和Java字段建立映射
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
