package org.lingZero.modularization_defend.AgreementCoreNetwork.api.transfer;

/**
 * 能量传输数据
 * 
 * @author Modularization Defend Team
 */
public class EnergyTransferData implements ITransferData {
    private double amount;
    
    public EnergyTransferData(double amount) {
        this.amount = amount;
    }
    
    @Override
    public DataType getType() {
        return DataType.ENERGY;
    }
    
    @Override
    public double getAmount() {
        return amount;
    }
    
    @Override
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
