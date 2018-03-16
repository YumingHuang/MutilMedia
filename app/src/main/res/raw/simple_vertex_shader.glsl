uniform mat4 uMVPMatrix;
attribute vec4 a_Position;

void main()
{
    gl_Position = uMVPMatrix * a_Position;
}