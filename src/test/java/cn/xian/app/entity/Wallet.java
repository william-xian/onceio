package cn.xian.app.entity;

import top.onceio.db.annotation.Col;
import top.onceio.db.annotation.Tbl;
import top.onceio.db.tbl.OEntity;


@Tbl(extend=UserChief.class,autoCreate=true)
public class Wallet extends OEntity<Long>{
    @Col(nullable = true)
	private int balance;
    @Col(nullable = true)
	private int expenditure;
    @Col(nullable = true)
	private int income;
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
	public int getExpenditure() {
		return expenditure;
	}
	public void setExpenditure(int expenditure) {
		this.expenditure = expenditure;
	}
	public int getIncome() {
		return income;
	}
	public void setIncome(int income) {
		this.income = income;
	}
}