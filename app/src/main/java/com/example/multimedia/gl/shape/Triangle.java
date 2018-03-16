package com.example.multimedia.gl.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.multimedia.R;
import com.example.multimedia.gl.ShaderHelper;
import com.example.multimedia.utils.GlShaderCodeReaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Triangle {

    private int mProgram;
    private Context mContext;
    private FloatBuffer vertexBuffer;

    /***  每个顶点的坐标数X, Y, Z, U, V */
    static float[] mTriangleCoords = {
            // top
            -1.0f, -0.5f, 0, 0.0f, 0.5f,
            // bottom left
            1.0f, -0.5f, 0, 0.5f, 0.0f,
            // bottom right
            0.0f, 1.0f, 0, 1.0f, 0.5f
    };

    /***  应用投影与相机视图 */
    private int muMVPMatrixHandle;
    /***  用于存储变换矩阵结果的总变换矩阵[4*4] */
    private float[] mMVPMatrix = new float[16];
    /***  [4*4]的视图变换矩阵(ViewMatrix) */
    private float[] mVMatrix = new float[16];
    /***  [4*4]的投影变换矩阵(ProjectionMatrix) */
    private float[] mProMatrix = new float[16];
    /***  [4*4]的模型变换矩阵(ModelViewMatrix) */
    private float[] mMMatrix = new float[16];
    public volatile float mAngle;

    private static final int BYTES_PER_FLOAT = 4;
    /*** 数组中每个顶点的坐标数 */
    private static final int COORDS_PER_VERTEX = 2;

    /*** 第一步 : 定义两个标签，分别于顶点、片段着色器代码中的变量名相同 */
    private static final String A_POSITION = "a_Position";
    private static final String U_COLOR = "u_Color";
    /*** 第二步: 定义两个ID,我们就是通ID来实现数据的传递的,这个与前面获得program的ID的含义类似的 */
    private int mColorHandle, mPostionHandle;
    /*** 第四步:定义坐标元素的个数，这里有三个顶点 */
    private static final int POSITION_COMPONENT_COUNT = 3;

    public Triangle(Context context) {
        this.mContext = context;

        vertexBuffer = ByteBuffer
                .allocateDirect(mTriangleCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        // 把坐标加入FloatBuffer中
        vertexBuffer.put(mTriangleCoords);
        // 设置buffer，从第一个坐标开始读
        vertexBuffer.position(0);

        getProgram();

        // 第三步: 获取这两个ID ，是通过前面定义的标签获得的
        mColorHandle = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        mPostionHandle = GLES20.glGetAttribLocation(mProgram, A_POSITION);

        //第五步: 传入数据
        GLES20.glVertexAttribPointer(mPostionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPostionHandle);
    }

    /**
     * 获取program用到OpenGL环境中
     */
    private void getProgram() {
        //获取顶点着色器文本
        String vertexShaderSource = GlShaderCodeReaderUtil
                .readTextFileFromResource(mContext, R.raw.simple_vertex_shader);
        //获取片段着色器文本
        String fragmentShaderSource = GlShaderCodeReaderUtil
                .readTextFileFromResource(mContext, R.raw.simple_fragment_shader);
        //获取program的id
        mProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);
        GLES20.glUseProgram(mProgram);
    }

    public void change(int width, int height) {
        float ratio = (float) width / height;
        //调用此方法来计算生成透视投影矩阵
        Matrix.frustumM(mProMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //当Surface改变时，获取指定着色器的uMVPMatrix参数
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //设定相机的视角
        //调用此方法产生摄像机9参数位置矩阵
        Matrix.setLookAtM(mVMatrix, 0,
                //相机的x,y,z坐标
                0, 0, -3,
                //目标对应的x,y,z坐标
                0, 0, 0,
                //相机的视觉向量(upx,upy,upz,三个向量最终的合成向量的方向为相机的方向)
                0, 1.0f, 1.0f
        );
    }

    public void preDraw() {
        //把左矩阵投影矩阵与右矩阵视图矩阵变换后的结果存储在总矩阵mMVPMatrix中
        Matrix.multiplyMM(mMVPMatrix, 0, mProMatrix, 0, mVMatrix, 0);

        //为三角形创建一个旋转动作
        /*long time = SystemClock.uptimeMillis() % 4000L;
        mAngle = 0.090f * ((int)time);*/
        //创建一个绕x,y,z轴旋转一定角度的矩阵
        Matrix.setRotateM(mMMatrix, 0, mAngle, 0, 0, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    /**
     * 第七步:绘制
     */
    public void draw() {
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform4f(mColorHandle, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, POSITION_COMPONENT_COUNT);
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }
}