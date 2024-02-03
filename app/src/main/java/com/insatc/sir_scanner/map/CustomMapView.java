package com.insatc.sir_scanner.map;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.osmdroid.tileprovider.MapTileProviderBase;


/* IMPORTANT :
Cette classe existe UNIQUEMENT pour empêcher le scroll de la page quand on navigue sur la carte.
Ne rien changer ici. Il y a tous les constructeurs au cas où.
 */
public class CustomMapView extends org.osmdroid.views.MapView{

    public CustomMapView(Context context, MapTileProviderBase tileProvider, Handler tileRequestCompleteHandler, AttributeSet attrs) {
        super(context, tileProvider, tileRequestCompleteHandler, attrs);
    }

    public CustomMapView(Context context, MapTileProviderBase tileProvider, Handler tileRequestCompleteHandler, AttributeSet attrs, boolean hardwareAccelerated) {
        super(context, tileProvider, tileRequestCompleteHandler, attrs, hardwareAccelerated);
    }

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMapView(Context context) {
        super(context);
    }

    public CustomMapView(Context context, MapTileProviderBase aTileProvider) {
        super(context, aTileProvider);
    }

    public CustomMapView(Context context, MapTileProviderBase aTileProvider, Handler tileRequestCompleteHandler) {
        super(context, aTileProvider, tileRequestCompleteHandler);
    }

    //C'est cette fonction qui est surchargée
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                System.out.println("unlocked");
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_DOWN:
                System.out.println("locked");
                this.getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

}
