package com.example.multimedia.gl;

import android.opengl.GLES20;
import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetShaderInfoLog;

public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    /**
     * 加载并编译顶点着色器
     *
     * @param shaderCode 着色器脚本程序
     * @return 返回openGl id
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 加载并编译片段着色器
     *
     * @param shaderCode 着色器脚本程序
     * @return 返回openGl id
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 加载并编译着色器
     *
     * @param type       着色器类型
     * @param shaderCode 着色器脚本程序
     * @return 返回openGl id
     */
    private static int compileShader(int type, String shaderCode) {
        // 建立新的着色器对象  
        final int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == 0) {
            Log.d(TAG, "不能创建新的着色器.");
            return 0;
        }
        // 传递着色器资源代码(文本形式).
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        // 编译着色器脚本程序
        GLES20.glCompileShader(shaderObjectId);
        // 获取编译的状态,来判断是否编译通过
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS,
                compileStatus, 0);
        Log.d(TAG, "代码编译结果:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId));
        // 确认编译的状态  
        if (compileStatus[0] == 0) {
            // 如果编译失败，则删除该对象  
            glDeleteShader(shaderObjectId);
            Log.d(TAG, "编译失败!");
            return 0;
        }
        // 返回着色器的openGl id
        return shaderObjectId;
    }

    /**
     * 链接顶点着色器和片段着色器成一个program
     *
     * @param vertexShaderId   vertexShaderId
     * @param fragmentShaderId fragmentShaderId
     * @return program的openGl id
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 新建一个program对象
        final int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            Log.d(TAG, "不能新建一个 program");
            return 0;
        }

        // Attach the vertex shader to the program.  
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        // Attach the fragment shader to the program.  
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        // 将两个着色器连接成一个program  
        GLES20.glLinkProgram(programObjectId);

        // 获取连接状态  
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        Log.d(TAG, "Results of linking program:\n" + glGetProgramInfoLog(programObjectId));

        // 验证连接状态  
        if (linkStatus[0] == 0) {
            // 连接失败
            GLES20.glDeleteProgram(programObjectId);
            Log.d(TAG, "连接 program 失败!.");
            return 0;
        }

        // Return the program object ID.  
        return programObjectId;
    }

    /**
     * Validates an OpenGL program. Should only be called when developing the
     * application.
     */
    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);

        Log.v(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog: " + GLES20.glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    /**
     * 编译，连接 ，返回program的ID
     *
     * @param vertexShaderSource   vertex着色器脚本程序
     * @param fragmentShaderSource fragment着色器脚本程序
     * @return 返回program的ID
     */
    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;
        // Compile the shader
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        // Link them into a shader program.  
        program = linkProgram(vertexShader, fragmentShader);
        validateProgram(program);
        return program;
    }
}