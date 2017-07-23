package lyh.seaweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lyh.seaweather.db.City;
import lyh.seaweather.db.County;
import lyh.seaweather.db.Province;
import lyh.seaweather.gson.Weather;

/**
 * Created by liuwei on 17-7-23.
 */

public class Utility {
    //解析和处理服务器省级数据
    public static boolean handleProvinceResponse(String reponse){
        if (!TextUtils.isEmpty(reponse)){
            try{
                JSONArray allProvinces=new JSONArray(reponse);
                for (int i=0;i<allProvinces.length();i++){
                    JSONObject provinceobject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceobject.getString("name"));
                    province.setProvinceCode(provinceobject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    //解析和处理服务器市级数据
    public static boolean handleCityResponse(String reponse,int provinceid){
        if (!TextUtils.isEmpty(reponse)){
            try {
                JSONArray allcitys=new JSONArray(reponse);
                for (int i=0;i<allcitys.length();i++){
                    JSONObject cityobject=allcitys.getJSONObject(i);
                    City city=new City();
                    city.setProvinceCode(provinceid);
                    city.setCityName(cityobject.getString("name"));
                    city.setCityCode(cityobject.getInt("id"));
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    //解析和处理服务器县级数据
    public static boolean handleCountyResponse(String reponse,int cityid){
        if (!TextUtils.isEmpty(reponse)){
            try{
                JSONArray allcountys=new JSONArray(reponse);
                for (int i=0;i<allcountys.length();i++){
                    JSONObject cityobject=allcountys.getJSONObject(i);
                    County county=new County();
                    county.setCityId(cityid);
                    county.setCountyName(cityobject.getString("name"));
                    county.setWeatherId(cityobject.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    //解析天气的数据
    public static Weather handleWeatherResponse(String reponse){
        try{
            JSONObject jsonObject=new JSONObject(reponse);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
