package com.example.multimedia.gl.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.multimedia.gl.shape.SquareV2;
import com.example.multimedia.gl.shape.TriangleV2;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private TriangleV2 mTriangle;
    private SquareV2 mSquare;

    /*** mMVPMatrix is an abbreviation for "Model View Projection Matrix" */
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float mAngle;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // 设置背景颜色,OpenGL支持两种颜色模式：一种是RGBA
        GLES20.glClearColor(255f, 255f, 255f, 1.0f);

        //定义一个三角形
        mTriangle = new TriangleV2();
        //定义一个正方形
     //   mSquare = new SquareV2();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        // 调整视点的几何变化,如屏幕旋转
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // 投影矩阵应用在坐标系中,当onDrawFrame方向调用时 ,投影矩阵调整坐标系统
         Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        // 画背景颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix) 创建一个模拟摄像机观察变换位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // 计算投影及视图变化
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // 绘制正方形
     //   mSquare.draw(mMVPMatrix);

        // 创建一个旋转三角形

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);

       // Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // 绘制三角形
        mTriangle.draw(scratch);
    }

    /**
     * Utility method for compiling a OpenGL shader.
     * <p>
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type       - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     * <p>
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     * <p>
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

}