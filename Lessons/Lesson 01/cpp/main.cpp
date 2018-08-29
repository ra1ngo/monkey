// GLEW нужно подключать до GLFW.
// GLEW
#define GLEW_STATIC
#include <GL/glew.h>
// GLFW
#include <GLFW/glfw3.h>
// Включаем стандартные заголовки
#include <stdio.h>
#include <stdlib.h>

int main(void){
    GLFWwindow* window;

    // Инициализируем GLFW
    if( !glfwInit() ){
        fprintf( stderr, "Ошибка при инициализации GLFWn" );
        return -1;
    }

    //glfwWindowHint(GLFW_FSAA_SAMPLES, 4); // 4x Сглаживание
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // Мы хотим использовать OpenGL 3.3
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Мы не хотим старый OpenGL
	glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);    //Выключение возможности изменения размера окна

    // Открыть окно и создать в нем контекст OpenGL
    window = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
    //GLFWwindow* window = glfwCreateWindow(800, 600, "LearnOpenGL", null, null);
    if( window == NULL ){
        fprintf( stderr, "Невозможно открыть окно GLFW. Если у вас Intel GPU, то он не поддерживает версию 3.3. Попробуйте версию уроков для OpenGL 2.1.n" );
        glfwTerminate();
        return -1;
    }
    /* Make the window's context current */
    glfwMakeContextCurrent(window);

    // Инициализируем GLEW
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

    // Включим режим отслеживания нажатия клавиш, для проверки ниже
    //glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);

    /* Loop until the user closes the window */
    while (/*glfwGetKey(window, GLFW_KEY_ESCAPE ) != GLFW_PRESS && */!glfwWindowShouldClose(window)){
        /* Render here */
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glfwPollEvents(); /* Poll for and process events */

        // Сбрасываем буферы
        glfwSwapBuffers(window); /* Swap front and back buffers */
    }

    glfwTerminate();
    return 0;
}
