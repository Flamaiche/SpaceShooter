package learnGL.tools;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    private String vertexCode;
    private String fragmentCode;

    public Shader(String vertexPath, String fragmentPath) {
        vertexCode = readFileFromResources(vertexPath);
        fragmentCode = readFileFromResources(fragmentPath);
        compile();
    }

    private String readFileFromResources(String fileName) {
        try (InputStream in = Shader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + fileName);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource: " + fileName, e);
        }
    }

    public int getProgramId() {
        return programId;
    }

    private void compile() {
        // Vertex shader
        vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderId, vertexCode);
        glCompileShader(vertexShaderId);
        if (glGetShaderi(vertexShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Erreur compilation vertex shader : " + glGetShaderInfoLog(vertexShaderId));
        }

        // Fragment shader
        fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId, fragmentCode);
        glCompileShader(fragmentShaderId);
        if (glGetShaderi(fragmentShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Erreur compilation fragment shader : " + glGetShaderInfoLog(fragmentShaderId));
        }

        // Programme
        programId = glCreateProgram();
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Erreur linkage shader : " + glGetProgramInfoLog(programId));
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, fragmentShaderId);
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        glDeleteProgram(programId);
    }

    public void setUniformMat4f(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void setUniform1f(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    public void setUniform2f(String name, float x, float y) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform2f(location, x, y);
        }
    }

    public void setUniform3f(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform3f(location, x, y, z);
        }
    }
}
