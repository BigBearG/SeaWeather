package lyh.seaweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lyh.seaweather.R;
import lyh.seaweather.db.City;
import lyh.seaweather.db.County;
import lyh.seaweather.db.Province;
import lyh.seaweather.util.HttpUtil;
import lyh.seaweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by liuwei on 17-7-23.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog mprogressDialog;
    private TextView mtitleText;
    private Button mbackButton;
    private ListView mlistview;
    private ArrayAdapter<String> madapter;
    private List<String> mdataList=new ArrayList<>();
    //省列表
    private List<Province> mprovinceList;
    //市列表
    private  List<City> mcityList;
    //县列表
    private List<County> mcountyList;
    //选中的省份
    private Province mselectedProvince;
    //选中的城市
    private City mselectedCity;
    //当前选中的级别
    private int mcurrentLevel;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);//获取布局文件，找到控件
        mtitleText = (TextView) view.findViewById(R.id.title_text);
        mbackButton = (Button) view.findViewById(R.id.back_button);
        mlistview = (ListView) view.findViewById(R.id.list_view);
        madapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,mdataList);//定义适配器
        mlistview.setAdapter(madapter);//为ListView设置适配器
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mcurrentLevel == LEVEL_PROVINCE) {
                    mselectedProvince = mprovinceList.get(position);//选中的省份
                    queryCities();
                } else if (mcurrentLevel == LEVEL_CITY) {
                    mselectedCity = mcityList.get(position);//选中的城市
                    queryCounties();
                }else if (mcurrentLevel==LEVEL_COUNTY){
                    String weatherId=mcountyList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
            });
            mbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mcurrentLevel == LEVEL_COUNTY) {
                    queryCities();//市级初始化
                } else if (mcurrentLevel == LEVEL_CITY) {
                    queryProvinces();//县级初始化
                }
            }
            });
            queryProvinces();//直接调用省级初始化

    }
    //查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
    private void queryProvinces() {
        mtitleText.setText("中国");//ListView标题
        mbackButton.setVisibility(View.GONE);//visible显示；invisible显示黑背景条；gone不显示，省级已经没有上级无需返回键
        mprovinceList = DataSupport.findAll(Province.class);//LitePal接口
        if (mprovinceList.size() > 0) {
            mdataList.clear();//如果有值，进行初始化数据，准备显示到ListView
            for (Province province : mprovinceList) {
                mdataList.add(province.getProvinceName());//高级for循环遍历数据
            }
            madapter.notifyDataSetChanged();//有时候我们需要修改已经生成的列表，
            // 添加或者修改数据，notifyDataSetChanged()可以在修改适配器绑定的数组后，
            // 不用重新刷新Activity，通知Activity更新ListView
            mlistview.setSelection(0);//
            mcurrentLevel = LEVEL_PROVINCE;//更改选中级别
        } else {//如果没有值，去服务器查询
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }
    //查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
    private void queryCities() {
        mtitleText.setText(mselectedProvince.getProvinceName());//通过选中省份获取名字
        mbackButton.setVisibility(View.VISIBLE);//显示
        mcityList = DataSupport.where("provinceid = ?", String.valueOf(mselectedProvince.getId())).find(City.class);//LitePal接口
        if (mcityList.size() > 0) {
            mdataList.clear();//如果有值，进行初始化数据，准备显示到ListView
            for (City city : mcityList) {
                mdataList.add(city.getCityName());//高级for循环遍历
            }
            madapter.notifyDataSetChanged();//更新ListVIew
            mlistview.setSelection(0);//传入一个index整型数值，就可以让ListView定位到指定Item的位置。
            mcurrentLevel = LEVEL_CITY;//设置操作级别
        } else {
            int provinceCode =mselectedProvince.getProvinceCode();//通过选择的省份获取id
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");//去服务器查询数据
        }
    }
    //查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
    private void queryCounties() {
        mtitleText.setText(mselectedCity.getCityName());//通过选择的城市获取name
        mbackButton.setVisibility(View.VISIBLE);//显示
        mcountyList = DataSupport.where("cityid = ?", String.valueOf(mselectedCity.getId())).find(County.class);
        if (mcountyList.size() > 0) {
            mdataList.clear();//如果有值，进行初始化数据，准备显示到ListView
            for (County county : mcountyList) {
                mdataList.add(county.getCountyName());//高级for循环遍历
            }
            madapter.notifyDataSetChanged();//更新ListVIew
            mlistview.setSelection(0);
            mcurrentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode =mselectedProvince.getProvinceCode();
            int cityCode = mselectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }
    //根据传入的地址和类型从服务器上查询省市县数据。
    private void queryFromServer(String address, final String type) {//type操作等级
        showProgressDialog();//显示进度条
        HttpUtil.sendOkHttpRequest(address, new Callback(){//向服务器发送数据请求
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);//省级解析
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, mselectedProvince.getId());//市级解析
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, mselectedCity.getId());//县级解析
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();//解析完成关闭进度条
                            if ("province".equals(type)) {
                                queryProvinces();//显示ListView
                            } else if ("city".equals(type)) {
                                queryCities();//显示ListView
                            } else if ("county".equals(type)) {
                                queryCounties();//显示ListView
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    //显示进度对话框
    private void showProgressDialog() {
        if (mprogressDialog == null) {
            mprogressDialog = new ProgressDialog(getActivity());
            mprogressDialog.setMessage("正在加载...");
            mprogressDialog.setCanceledOnTouchOutside(false);
        }
        mprogressDialog.show();
    }
    //关闭进度对话框
    private void closeProgressDialog() {
        if (mprogressDialog != null) {
            mprogressDialog.dismiss();
        }
    }

}
