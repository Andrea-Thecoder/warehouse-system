package it.warehouse.transport.model.enumerator;

public enum StockAction {
    INCREASE(-1),
    DECREASE(1);


    private final int factor;
    StockAction (int factor){
        this.factor = factor;
    }

    public double apply(double value){
        return factor*value;
    }



}
