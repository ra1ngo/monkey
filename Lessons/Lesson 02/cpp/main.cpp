// GLEW ����� ���������� �� GLFW.
// GLEW
//#define GLEW_STATIC
#include <GL/glew.h>
// GLFW
#include <GLFW/glfw3.h>
// �������� ����������� ���������
#include <stdio.h>
#include <stdlib.h>

int main(void){
    GLFWwindow* window;

    // �������������� GLFW
    if( !glfwInit() ){
        fprintf( stderr, "������ ��� ������������� GLFWn" );
        return -1;
    }

    //glfwWindowHint(GLFW_FSAA_SAMPLES, 4); // 4x �����������
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // �� ����� ������������ OpenGL 3.3
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // �� �� ����� ������ OpenGL
	glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);    //���������� ����������� ��������� ������� ����

    // ������� ���� � ������� � ��� �������� OpenGL
    window = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
    //GLFWwindow* window = glfwCreateWindow(800, 600, "LearnOpenGL", null, null);
    if( window == NULL ){
        fprintf( stderr, "���������� ������� ���� GLFW. ���� � ��� Intel GPU, �� �� �� ������������ ������ 3.3. ���������� ������ ������ ��� OpenGL 2.1.n" );
        glfwTerminate();
        return -1;
    }
    /* Make the window's context current */
    glfwMakeContextCurrent(window);

    // �������������� GLEW
    GLenum err = glewInit();
    if (GLEW_OK != err){
      /* Problem: glewInit failed, something is seriously wrong. */
      fprintf(stderr, "Error: %s\n", glewGetErrorString(err));

    }
    fprintf(stdout, "Status: Using GLEW %s\n", glewGetString(GLEW_VERSION));

    //Viewport
    int width, height;
    glfwGetFramebufferSize(window, &width, &height);
    glViewport(0, 0, width, height);

    // ������� ����� ������������ ������� ������, ��� �������� ����
    //glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);

    //������ ������ ������������
    GLfloat vertices[] = {
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.0f,  0.5f, 0.0f
    };

    //������� ���������� ������ (vertex buffer objects)
    GLuint VBO; // ��� ����� ��������������� ������ ������ ������
    glGenBuffers(1, &VBO); // �������� 1 ����� � �������� � ���������� VBO ��� �������������
    glBindBuffer(GL_ARRAY_BUFFER, VBO); // ������� ������ ��� ��������� ����� �������

    // ��������� ���������� � �������� � OpenGL
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    //glDeleteBuffers(1, &vbo); //�������� VBO

    //Vertex Array Object!!!
    GLuint VertexArrayID;
    glGenVertexArrays(1, &VertexArrayID);
    glBindVertexArray(VertexArrayID);

    // ���������, ��� ������ ������� ��������� ����� �������
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(
       0,                  // ������� 0. ��������� �� ���� ����� ���������� � �����, ����������� ��������.
       3,                  // ������
       GL_FLOAT,           // ���
       GL_FALSE,           // ���������, ��� �������� �� �������������
       0,                  // ���
       (void*)0            // �������� ������� � ������
    );

    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(window)){
        /* Render here */
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);


        // ������� �����������!
        glDrawArrays(GL_TRIANGLES, 0, 3); // ������� � ������� 0, ����� 3 ������� -> ���� �����������

        glfwPollEvents(); /* Poll for and process events */
        // ���������� ������
        glfwSwapBuffers(window); /* Swap front and back buffers */
    }

    glfwTerminate();
    return 0;
}
