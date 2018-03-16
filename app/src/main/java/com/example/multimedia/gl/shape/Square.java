package com.example.multimedia.gl.shape;

import android.content.Context;
import android.opengl.GLES20;

import com.example.multimedia.R;
import com.example.multimedia.gl.ShaderHelper;
import com.example.multimedia.utils.GlShaderCodeReaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

    private int mProgram;
    private Context mContext;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    /*** number of coordinates per vertex in this array */
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            // top left
            -0.5f, 0.5f, 0.0f,
            // bottom left
            -0.5f, -0.5f, 0.0f,
            // bottom right
            0.5f, -0.5f, 0.0f,
            // top right
            0.5f, 0.5f, 0.0f};

    /*** order to draw vertices */
    private short drawOrder[] = {0, 1, 2, 0, 2, 3};

    /*** 第一步 : 定义两个标签，分别于顶点、片段着色器代码中的变量名相同 */
    private static final String A_POSITION = "a_Position";
    private static final String U_COLOR = "u_Color";
    /*** 第二步: 定义两个ID,我们就是通ID来实现数据的传递的,这个与前面获得program的ID的含义类似的 */
    private int uColorLocation, aPositionLocation;
    /*** 第四步:定义坐标元素的个数，这里有4个顶点 */
    private static final int POSITION_COMPONENT_COUNT = 4;

    public Square(Context context) {
        this.mContext = context;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        getProgram();
        // 第三步: 获取这两个ID ，是通过前面定义的标签获得的
        uColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);

        //第五步: 传入数据
        GLES20.glVertexAttribPointer(aPositionLocation, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
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

    public void draw() {
        // Set color for drawing the shape
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.5f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, POSITION_COMPONENT_COUNT);
    }
}