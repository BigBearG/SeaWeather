package lyh.seaweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import lyh.seaweather.gson.Forecast;
import lyh.seaweather.gson.Weather;
import lyh.seaweather.service.AutoUpdateService;
import lyh.seaweather.util.HttpUtil;
import lyh.seaweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout mdrawerLayout;

    private ScrollView mweatherLayout;

    private Button mnavButton;

    private TextView mtitleCity;

    private TextView mtitleUpdateTime;

    private TextView mdegreeText;

    private TextView mweatherInfoText;

    private LinearLayout mforecastLayout;

    private TextView maqiText;

    private TextView mpm25Text;

    private TextView mcomfortText;

    private TextView mcarWashText;

    private TextView msportText;

    private ImageView mbingPicImg;
    private String mweatherId;
    public SwipeRefreshLayout mswipeRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各控件
        mweatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        mtitleCity = (TextView) findViewById(R.id.title_city);
        mtitleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        mdegreeText = (TextView) findViewById(R.id.degree_text);
        mweatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        mforecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        maqiText = (TextView) findViewById(R.id.aqi_text);
        mpm25Text = (TextView) findViewById(R.id.pm25_text);
        mcomfortText = (TextView) findViewById(R.id.comfort_text);
        mcarWashText = (TextView) findViewById(R.id.car_wash_text);
        msportText = (TextView) findViewById(R.id.sport_text);
        mbingPicImg= (ImageView) findViewById(R.id.bing_pic_img);
        mswipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mswipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mdrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        mnavButton= (Button) findViewById(R.id.nav_button);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String bingPic=prefs.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(mbingPicImg);
        }else {
            loadBingPic();
        }
        mnavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mweatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            mweatherId = getIntent().getStringExtra("weather_id");
            mweatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mweatherId);
        }
        mswipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mweatherId);
            }
        });
    }
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mbingPicImg);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mweatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        mswipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        mswipeRefresh.setRefreshing(false);

                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mtitleCity.setText(cityName);
        mtitleUpdateTime.setText(updateTime);
        mdegreeText.setText(degree);
        mweatherInfoText.setText(weatherInfo);
        mforecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, mforecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            mforecastLayout.addView(view);
        }
        if (weather != null && "ok".equals(weather.status)) {
            Intent intent=new Intent(WeatherActivity.this,AutoUpdateService.class);
            startService(intent);
        }
        if (weather.aqi != null) {
            maqiText.setText(weather.aqi.city.aqi);
            mpm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        mcomfortText.setText(comfort);
        mcarWashText.setText(carWash);
        msportText.setText(sport);
        mweatherLayout.setVisibility(View.VISIBLE);
    }
}
