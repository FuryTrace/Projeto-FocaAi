package com.example.focaai.utils;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class AlertaSonoro {

    private static ToneGenerator gerador;

    private static ToneGenerator obterGerador() {
        if (gerador == null) {
            gerador = new ToneGenerator(
                    AudioManager.STREAM_NOTIFICATION,
                    100
            );
        }
        return gerador;
    }

    public static void tocarFimDoFoco() {
        obterGerador().startTone(ToneGenerator.TONE_PROP_ACK, 250);
    }

    public static void tocarFimDaPausa() {
        obterGerador().startTone(ToneGenerator.TONE_PROP_BEEP, 180);
    }

    public static void tocarSessaoConcluida() {
        obterGerador().startTone(ToneGenerator.TONE_PROP_ACK, 400);
    }

    public static void liberar() {
        if (gerador != null) {
            gerador.release();
            gerador = null;
        }
    }
}