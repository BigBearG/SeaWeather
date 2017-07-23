package lyh.seaweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by liuwei on 17-7-22.
 */

public class City extends DataSupport {
    private int id;
    private String cityName;
    private int cityCode;
    private int provinceID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceCode() {
        return provinceID;
    }

    public void setProvinceCode(int provinceID) {
        this.provinceID = provinceID;
    }
}
