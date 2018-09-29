#include "monkey.h"

int main(void){

    Monkey *monk = new Monkey();
    Monkey::Screen *mainScreen = new Monkey::Screen();
    monk->setScreen(mainScreen);

    Monkey::Texture *texture = new Monkey::Texture("res/elli_walk.png");


    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(mainScreen->context)){
        /* Render here */


        //glEnable(GL_DEPTH_TEST); // включает использование буфера глубины
        //glDepthFunc(GL_LEQUAL); // определяет работу буфера глубины: более ближние объекты перекрывают дальние
        //glDepthFunc(GL_LESS);// Фрагмент будет выводиться только в том, случае, если он находится ближе к камере, чем предыдущий



        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);




        glfwPollEvents();
        // Сбрасываем буферы
        glfwSwapBuffers(mainScreen->context);
    }

    glfwTerminate();
    return 0;
}
