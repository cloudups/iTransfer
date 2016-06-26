package cn.edu.sdust.silence.itransfer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.handler.SendActivityHandler;
import cn.edu.sdust.silence.itransfer.reciever.WifiP2PBroadcastReceiver;
import cn.edu.sdust.silence.itransfer.thread.ServerManager;
import cn.edu.sdust.silence.itransfer.thread.ServerManager2;
import cn.edu.sdust.silence.itransfer.ui.actionbutton.FloatingActionButton;
import cn.edu.sdust.silence.itransfer.ui.loading.RotateLoading;
import cn.edu.sdust.silence.itransfer.ui.progress.NumberProgressBar;
import cn.edu.sdust.silence.itransfer.ui.scan.been.Info;
import cn.edu.sdust.silence.itransfer.ui.scan.custom.RadarViewGroup;

/**
 * create by shifeiqi
 */
public class SendActivity extends Activity implements RadarViewGroup.IRadarClickListener {

    private RadarViewGroup radarViewGroup;
    private SparseArray<Info> mDatas = new SparseArray<>();

    //view
    private NumberProgressBar progress;
    private RotateLoading loading;
    private TextView tv_point;
    private View layout_point_container;

    //WIFI p2p
    public static int PROGRESS = 1;

    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pInfo info;
    private String filePath;
    private boolean isConnectIp = false;
    private SendActivityHandler sendActivityHandler;


    //设备列表
    private List<WifiP2pDevice> peers = new ArrayList();
    private Map<String, Long> map = new HashMap<>();

    private FloatingActionButton sendToComputerBtn;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();
        getFilePath();
        initIntentFilter();
        initWifiP2p();
        DiscoverPeers();
    }

    private void getFilePath() {
        Intent intent = getIntent();

        if (Intent.ACTION_SEND == intent.getAction()) {
            Bundle bundle = intent.getExtras();
            Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
            filePath = uri.getPath();
//            Toast.makeText(SendActivity.this, "" + uri.getPath() + "  " + intent.getAction(), Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_VIEW == intent.getAction()) {
            Uri uri = intent.getData();
            filePath = uri.getPath();
//            Toast.makeText(SendActivity.this, "" + uri.getPath() + "  " + intent.getAction(), Toast.LENGTH_LONG).show();
        } else {
            filePath = intent.getStringExtra("path");
        }
    }

    private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initWifiP2p() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                mDatas.clear();
                for (int i = 0; i < peers.size(); i++) {
                    WifiP2pDevice d = peers.get(i);
                    Info info = new Info();
                    info.setName(d.deviceName);
                    info.setPortraitId(R.drawable.icon_receice);
                    info.setAge(((int) Math.random() * 25 + 16) + "岁");
                    info.setSex(true);

                    if (map.keySet().contains(d.deviceName)) {
                        info.setDistance(map.get(d.deviceName));
                    } else {
                        long dis = Math.round((Math.random() * 10) * 100) / 100;
                        map.put(d.deviceName, dis);
                        info.setDistance(dis);
                    }

                    mDatas.put(i, info);
                }
                radarViewGroup.setDatas(mDatas);
            }
        };

        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo minfo) {
                tv_point.setText("连接成功，正在准备数据");
                info = minfo;
                if (info.groupFormed && info.isGroupOwner && !isConnectIp) {
                    ServerManager manager = new ServerManager(sendActivityHandler, filePath);
                    manager.start();
                    isConnectIp = true;
                    Log.i("xyz", "send create sucess");
                } else if (info.groupFormed && !info.isGroupOwner && !isConnectIp) {
                    ServerManager2 server = new ServerManager2(sendActivityHandler, info.groupOwnerAddress.getHostAddress(), filePath);
                    server.start();
                    isConnectIp = true;
                    Log.i("xyz", "send create sucess");
                }
            }
        };

        mReceiver = new WifiP2PBroadcastReceiver(mManager, mChannel, this, mPeerListListerner, mInfoListener);
    }

    private void CreateConnect(String address) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.groupOwnerIntent = 0;
        config.wps.setup = WpsInfo.PBC;

        Log.i("xyz", "other address : " + address);
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                if (info != null) {
                    tv_point.setText("连接成功，正在准备数据");
                    Log.d("mac connect", "sucess");
                    Log.d("xyz", "isGroupOwner " + info.isGroupOwner + "");
                    Log.d("xyz", "groupOwner ip " + info.groupOwnerAddress.getHostAddress());
                }
            }

            @Override
            public void onFailure(int reason) {
                Log.d("mac connect", "fail");
            }
        });
    }

    private void startServer() {

        if (info.groupFormed && info.isGroupOwner && !isConnectIp) {
            ServerManager manager = new ServerManager(sendActivityHandler, filePath);
            manager.start();
            isConnectIp = true;
            Log.i("xyz", "send create sucess");
        } else if (info.groupFormed && !info.isGroupOwner && !isConnectIp) {
            ServerManager2 server = new ServerManager2(sendActivityHandler, info.groupOwnerAddress.getHostAddress(), filePath);
            server.start();
            isConnectIp = true;
            Log.i("xyz", "send create sucess");
        }
    }

    private void initView() {
        radarViewGroup = (RadarViewGroup) findViewById(R.id.radar);
        radarViewGroup.setiRadarClickListener(this);
        progress = (NumberProgressBar) findViewById(R.id.progress);
        progress.setProgressTextColor(Color.WHITE);
        progress.setReachedBarColor(Color.WHITE);
        progress.setUnreachedBarColor(Color.GRAY);
        progress.setProgressTextSize(40f);
        loading = (RotateLoading) findViewById(R.id.loading);
        tv_point = (TextView) findViewById(R.id.tv_point);
        layout_point_container = findViewById(R.id.layout_point_container);
        back = (ImageButton) findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定要取消发送文件吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        sendActivityHandler = new SendActivityHandler(SendActivity.this);
        layout_point_container.setVisibility(View.VISIBLE);
        loading.setLoadingColor(Color.WHITE);
        loading.start();
        tv_point.setText("点击对方头像进行文件发送");

        progress.setMax(100);

        sendToComputerBtn = (FloatingActionButton) findViewById(R.id.action_to_computer);
        sendToComputerBtn.setIcon(R.drawable.icon_pc);
        sendToComputerBtn.setColorNormalResId(R.color.button_normal);
        sendToComputerBtn.setColorPressedResId(R.color.button_pressed);
        sendToComputerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
                builder.setTitle("请选择文件发送模式");
                LayoutInflater inflater = LayoutInflater.from(SendActivity.this);
                View view = inflater.inflate(R.layout.chose_to_computer_dialog, null);
                view.findViewById(R.id.have).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SendActivity.this, SendToComputerActivity.class);
                        intent.putExtra("path", filePath);
                        startActivity(intent);
                        finish();
                    }
                });

                view.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SendActivity.this, FtpManagerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setView(view);
                builder.show();
            }
        });
    }

    /**
     * 开启发现节点
     */
    private void DiscoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("确定要取消发送文件吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 更新进度
     *
     * @param p
     */
    public void freshProgress(int p) {
        layout_point_container.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progress.setProgress(p);
        if (p >= 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("文件发送成功！");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    @Override
    public void onRadarItemClick(final int position) {
        if (!isConnectIp) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("确定要将文件发送给 " + mDatas.get(position).getName() + " 吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tv_point.setText("正在连接");
                    if (info == null)
                        CreateConnect(peers.get(position).deviceAddress);
                    else
                        Toast.makeText(SendActivity.this, "设备已连接，正在启用发送文件", Toast.LENGTH_LONG).show();

                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            Toast.makeText(SendActivity.this, "已经连接至某一台设备", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(mReceiver, mFilter);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mManager.cancelConnect(mChannel, null);
        mManager.removeGroup(mChannel, null);
        super.onDestroy();
    }
}
