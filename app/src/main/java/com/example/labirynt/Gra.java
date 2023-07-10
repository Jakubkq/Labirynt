package com.example.labirynt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class Gra extends View {

    private enum Direction{
        GORA, DUL, LEWO, PRAWO
    }

    private Komorka[][] komm;
    private Komorka gracz, wyjscie;
    private static final int KOLUMNA = 7,  WIERSZ=13;
    private static final float grSciany = 10;
    private float wielkosc, wysMarginesu, szerMarginesu;
    private Paint kolSciany, kolGracza, kolWyjscia;
    private Random random;

    public Gra(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        kolSciany = new Paint();
        kolSciany.setColor(Color.GREEN);
        kolSciany.setStrokeWidth(grSciany);

        kolGracza = new Paint();
        kolGracza.setColor(Color.BLUE);

        kolWyjscia = new Paint();
        kolWyjscia.setColor(Color.YELLOW);

        random = new Random();

        labirynt();
    }

    private Komorka getNeighbour(Komorka kom){
        ArrayList<Komorka> sas = new ArrayList<>();

        //left neighbour
        if(kom.kolumna > 0)
            if(!komm[kom.kolumna-1][kom.wiersz].odwiedzajacy)
                sas.add(komm[kom.kolumna-1][kom.wiersz]);

        //right neighbour
        if(kom.kolumna < KOLUMNA-1)
            if(!komm[kom.kolumna+1][kom.wiersz].odwiedzajacy)
                sas.add(komm[kom.kolumna+1][kom.wiersz]);

        //top neighbour
        if(kom.wiersz > 0)
            if(!komm[kom.kolumna][kom.wiersz-1].odwiedzajacy)
                sas.add(komm[kom.kolumna][kom.wiersz-1]);

        //bottom neighbour
        if(kom.wiersz < WIERSZ-1)
            if(!komm[kom.kolumna][kom.wiersz+1].odwiedzajacy)
                sas.add(komm[kom.kolumna][kom.wiersz+1]);

        if (sas.size() > 0) {
            int index = random.nextInt(sas.size());
            return sas.get(index);
        }
        return null;
    }

    private void usunSciane (Komorka obecny, Komorka nastepny){
        if(obecny.kolumna == nastepny.kolumna && obecny.wiersz == nastepny.wiersz+1){
            obecny.gorSciana = false;
            nastepny.dolSciana = false;
        }

        if(obecny.kolumna == nastepny.kolumna && obecny.wiersz == nastepny.wiersz-1){
            obecny.dolSciana = false;
            nastepny.gorSciana = false;
        }

        if(obecny.kolumna == nastepny.kolumna+1 && obecny.wiersz == nastepny.wiersz){
            obecny.lewSciana = false;
            nastepny.praSciana= false;
        }

        if(obecny.kolumna == nastepny.kolumna-1 && obecny.wiersz == nastepny.wiersz){
            obecny.praSciana = false;
            nastepny.lewSciana = false;
        }
    }


    private void labirynt(){
        Stack<Komorka> stack = new Stack<>();
        Komorka obecny, nastepny;

        komm = new Komorka[KOLUMNA][WIERSZ];

        for(int x=0; x<KOLUMNA; x++){
            for(int y=0; y<WIERSZ; y++){
                komm[x][y] = new Komorka(x, y);
            }
        }

        gracz = komm[0][0];
        wyjscie = komm[KOLUMNA-1][WIERSZ-1];

        obecny = komm[0][0];
        obecny.odwiedzajacy = true;
        do {
            nastepny = getNeighbour(obecny);
            if (nastepny != null) {
                usunSciane(obecny, nastepny);
                stack.push(obecny);
                obecny = nastepny;
                obecny.odwiedzajacy = true;
            } else
                obecny = stack.pop();
        }while(!stack.empty());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        int szerokosc = getWidth();
        int wysokosc = getHeight();

        if (szerokosc/wysokosc < KOLUMNA/WIERSZ)
            wielkosc = szerokosc/(KOLUMNA+1);
        else
            wielkosc = wysokosc/(WIERSZ+1);

        wysMarginesu = (szerokosc-KOLUMNA*wielkosc)/2;
        szerMarginesu = (wysokosc-WIERSZ*wielkosc)/2;

        canvas.translate(wysMarginesu, szerMarginesu);

        for(int x=0; x<KOLUMNA; x++){
            for(int y=0; y<WIERSZ; y++){
                if(komm[x][y].gorSciana)
                    canvas.drawLine(
                            x*wielkosc,
                            y*wielkosc,
                            (x+1)*wielkosc,
                            y*wielkosc,
                            kolSciany);

                if(komm[x][y].lewSciana)
                    canvas.drawLine(
                            x*wielkosc,
                            y*wielkosc,
                            x*wielkosc,
                            (y+1)*wielkosc,
                            kolSciany);

                if(komm[x][y].dolSciana)
                    canvas.drawLine(
                            x*wielkosc,
                            (y+1)*wielkosc,
                            (x+1)*wielkosc,
                            (y+1)*wielkosc,
                            kolSciany);

                if(komm[x][y].praSciana)
                    canvas.drawLine(
                            (x+1)*wielkosc,
                            y*wielkosc,
                            (x+1)*wielkosc,
                            (y+1)*wielkosc,
                            kolSciany);

            }
        }

        float margines = wielkosc/10;

        canvas.drawRect(
                gracz.kolumna*wielkosc+margines,
                gracz.wiersz*wielkosc+margines,
                (gracz.kolumna+1)*wielkosc-margines,
                (gracz.wiersz+1)*wielkosc-margines,
                kolGracza);

        canvas.drawRect(
                wyjscie.kolumna*wielkosc+margines,
                wyjscie.wiersz*wielkosc+margines,
                (wyjscie.kolumna+1)*wielkosc-margines,
                (wyjscie.wiersz+1)*wielkosc-margines,
                kolGracza);
    }

    private void movePlayer(Direction direction){
        switch (direction){
            case GORA:
                if(!gracz.gorSciana)
                    gracz = komm[gracz.kolumna][gracz.wiersz-1];
                break;
            case DUL:
                if(!gracz.dolSciana)
                    gracz = komm[gracz.kolumna][gracz.wiersz+1];
                break;
            case LEWO:
                if(!gracz.lewSciana)
                    gracz = komm[gracz.kolumna-1][gracz.wiersz];
                break;
            case PRAWO:
                if(!gracz.praSciana)
                    gracz = komm[gracz.kolumna+1][gracz.wiersz];
                break;
        }

        checkExit();
        invalidate();
    }

    private void checkExit(){
        if(gracz == wyjscie)
            labirynt();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
            return true;

        if (event.getAction() == MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = wysMarginesu + (gracz.kolumna+0.5f)*wielkosc;
            float playerCenterY = szerMarginesu + (gracz.wiersz+0.5f)*wielkosc;

            float fx = x - playerCenterX;
            float fy = y - playerCenterY;

            float absDx = Math.abs(fx);
            float absDy = Math.abs(fy);

            if(absDx > wielkosc  || absDy > wielkosc){

                if(absDx > absDy){
                    //move in x-direction
                    if(fx > 0)
                        movePlayer(Direction.PRAWO);
                    else
                        movePlayer(Direction.LEWO);
                }
                else{
                    //move in y-direction
                    if(fy > 0)
                        movePlayer(Direction.DUL);
                    else
                        movePlayer(Direction.GORA);
                }
            }
            return true;
        }


        return super.onTouchEvent(event);
    }

    private class Komorka{
        boolean
                gorSciana = true,
                lewSciana = true,
                dolSciana = true,
                praSciana = true,
                odwiedzajacy = false;
        int kolumna, wiersz;

        public Komorka(int kolumna, int wiersz) {
            this.kolumna = kolumna;
            this.wiersz = wiersz;
        }
    }
}
