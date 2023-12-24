package legacystore.store.legacytnt;

public class TipoTNT {

    private boolean animacao;
    private int tntsNaAnimacao;

    public TipoTNT(boolean animacao, int tntsNaAnimacao) {
        this.animacao = animacao;
        this.tntsNaAnimacao = tntsNaAnimacao;
    }

    public boolean isAnimacao() {
        return animacao;
    }

    public int getTntsNaAnimacao() {
        return tntsNaAnimacao;
    }
}
