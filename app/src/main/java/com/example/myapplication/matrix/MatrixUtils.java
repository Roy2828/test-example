package com.example.myapplication.matrix;

import android.app.Application;

import com.tencent.matrix.BuildConfig;
import com.tencent.matrix.Matrix;
import com.tencent.matrix.iocanary.IOCanaryPlugin;
import com.tencent.matrix.iocanary.config.IOConfig;
import com.tencent.matrix.memory.canary.MemoryCanaryPlugin;
import com.tencent.matrix.trace.TracePlugin;
import com.tencent.matrix.trace.config.TraceConfig;

/**
 * @author: njb
 * @date: 2023/8/10 11:19
 * @desc:
 */
public class MatrixUtils {
    private static volatile MatrixUtils INSTANCE;
    private static final String TAG = "MatrixLog";

    private MatrixUtils() {
    }

    public static MatrixUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (MatrixUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MatrixUtils();
                }
            }
        }
        return INSTANCE;
    }

    public void initPlugin(Application application, String splashActivity) {
        Matrix.Builder builder = new Matrix.Builder(application); // build matrix
        builder.pluginListener(new MatrixPluginListener(application)); // add general pluginListener
        MatrixDynamicConfigImpl matrixDynamicConfig = new MatrixDynamicConfigImpl(); // dynamic config
        boolean fpsEnable = matrixDynamicConfig.isFPSEnable();
        boolean traceEnable = matrixDynamicConfig.isTraceEnable();
        //Trace plugin
        TraceConfig traceConfig = new TraceConfig.Builder()
                .dynamicConfig(matrixDynamicConfig)
                .enableFPS(fpsEnable)//帧率
                .enableEvilMethodTrace(traceEnable)//慢方法
                .enableAnrTrace(traceEnable)//anr
                .enableStartup(traceEnable)//启动速度
                .splashActivities(splashActivity)//启动页    可以不设置
                //debug模式
                .isDebug(BuildConfig.DEBUG)
                //dev环境
                .isDevEnv(BuildConfig.DEBUG)
                .build();

        TracePlugin tracePlugin = new TracePlugin(traceConfig);
        builder.plugin(tracePlugin);

        MemoryCanaryPlugin memoryCanaryPlugin = new MemoryCanaryPlugin();
        builder.plugin(memoryCanaryPlugin);

        // io plugin
        IOCanaryPlugin ioCanaryPlugin = new IOCanaryPlugin(new IOConfig.Builder()
                .dynamicConfig(matrixDynamicConfig)
                .build());
        builder.plugin(ioCanaryPlugin);

        //init matrix
        Matrix.init(builder.build());
        tracePlugin.start();
    }
}
