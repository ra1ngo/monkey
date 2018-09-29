#include "monkey.h"

int main(void){

    Monkey *monk = new Monkey();
    Monkey::Screen *mainScreen = new Monkey::Screen();
    monk->setScreen(mainScreen);

    Monkey::Texture *texture = new Monkey::Texture("res/elli_walk.png");


    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(mainScreen->context)){
        /* Render here */


        //glEnable(GL_DEPTH_TEST); // РІРєР»СЋС‡Р°РµС‚ РёСЃРїРѕР»СЊР·РѕРІР°РЅРёРµ Р±СѓС„РµСЂР° РіР»СѓР±РёРЅС‹
        //glDepthFunc(GL_LEQUAL); // РѕРїСЂРµРґРµР»СЏРµС‚ СЂР°Р±РѕС‚Сѓ Р±СѓС„РµСЂР° РіР»СѓР±РёРЅС‹: Р±РѕР»РµРµ Р±Р»РёР¶РЅРёРµ РѕР±СЉРµРєС‚С‹ РїРµСЂРµРєСЂС‹РІР°СЋС‚ РґР°Р»СЊРЅРёРµ
        //glDepthFunc(GL_LESS);// Р¤СЂР°РіРјРµРЅС‚ Р±СѓРґРµС‚ РІС‹РІРѕРґРёС‚СЊСЃСЏ С‚РѕР»СЊРєРѕ РІ С‚РѕРј, СЃР»СѓС‡Р°Рµ, РµСЃР»Рё РѕРЅ РЅР°С…РѕРґРёС‚СЃСЏ Р±Р»РёР¶Рµ Рє РєР°РјРµСЂРµ, С‡РµРј РїСЂРµРґС‹РґСѓС‰РёР№



        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);




        glfwPollEvents();
        // РЎР±СЂР°СЃС‹РІР°РµРј Р±СѓС„РµСЂС‹
        glfwSwapBuffers(mainScreen->context);
    }

    glfwTerminate();
    return 0;
}
