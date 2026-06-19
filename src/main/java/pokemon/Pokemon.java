package pokemon;

import interfaces.Battleable;
import moves.*;
import types.TypeChart;

import java.util.ArrayList;

public abstract class Pokemon implements Battleable {

    private final int id;
    private final String name;
    private final MoveType type1;
    private final MoveType type2;
    private final int total;

    private final int maxHp;
    private int currentHp;

    private final int attack;
    private final int defense;
    private final int spAttack;
    private final int spDefense;
    private final int speed;

    private final ArrayList<Move> moves = new ArrayList<>();

    public Pokemon(int id, String name, MoveType type1, MoveType type2, int total, int hp, int attack, int defense, int spAttack, int spDefense, int speed) {
        if (hp <= 0) {
            throw new IllegalArgumentException("HP must be greater than 0.");
        }
        if (attack < 0 || defense < 0 || spAttack < 0 || spDefense < 0 || speed < 0) {
            throw new IllegalArgumentException("Statistics cannot be negative.");
        }

        this.id = id;
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.total = total;

        this.maxHp = hp;
        this.currentHp = hp;

        this.attack = attack;
        this.defense = defense;
        this.spAttack = spAttack;
        this.spDefense = spDefense;
        this.speed = speed;

        buildMoves();
    }

    private void buildMoves() {

        moves.add(createDefenseMove());

        ArrayList<Move> type1Moves = TypeMoves.getMoves(type1);

        ArrayList<Move> type2Moves = null;

        if (type2 != null) {

            type2Moves = TypeMoves.getMoves(type2);
        }

        if (attack >= spAttack) {

            moves.add(type1Moves.get(0));
        }
        else {

            moves.add(type1Moves.get(1));
        }

        if (type2 != null) {

            if (attack >= spAttack) {

                moves.add(type2Moves.get(0));
            }
            else {

                moves.add(type2Moves.get(1));
            }
        }
        else {

            moves.add(type1Moves.get(2));
        }

        if (attack >= spAttack) {

            moves.add(type1Moves.get(1));
        }
        else {

            moves.add(type1Moves.get(0));
        }
    }

    private Move createDefenseMove() {

        return new Move(
                "Defend",
                MoveType.NORMAL,
                MoveCategory.DEFENSE,
                0
        );
    }

    @Override
    public void useMove(int index, Battleable target) {

        Pokemon enemy = (Pokemon) target;

        Move move = moves.get(index);

        System.out.println("\n" + name + " used " + move.getName() + "!");

        if (move.getCategory() == MoveCategory.DEFENSE) {
            System.out.println(name + " is defending!");
            return;
        }

        int damage;

        if (move.getCategory() == MoveCategory.PHYSICAL) {
            damage = attack + move.getPower();
        }
        else {
            damage = spAttack + move.getPower();
        }

        double multiplier = calculateEffectiveness(move, enemy);

        damage *= multiplier;

        showEffectivenessMessage(multiplier);

        if (multiplier == 0) {
            return;
        }

        if (move.getCategory() == MoveCategory.PHYSICAL) {
            enemy.receiveDamage(damage);
        }
        else {
            enemy.receiveSpecialDamage(damage);
        }
    }

    private double calculateEffectiveness(Move move, Pokemon enemy) {

        double multiplier = TypeChart.getMultiplier(move.getType(), enemy.getType1());

        if (enemy.getType2() != null) {

            multiplier *= TypeChart.getMultiplier(move.getType(), enemy.getType2());
        }

        return multiplier;
    }

    private void showEffectivenessMessage(double multiplier) {

        if (multiplier == 0) {

            System.out.println("It doesn't affect...");
        }

        else if (multiplier > 1) {

            System.out.println("It's super effective!");
        }

        else if (multiplier < 1) {

            System.out.println("It's not very effective...");
        }
    }

    public void receiveDamage(int damage) {

        int finalDamage = damage - defense;

        if (finalDamage < 1) {
            finalDamage = 0;
        }

        currentHp -= finalDamage;

        if (currentHp < 0) {
            currentHp = 0;
        }

        System.out.println(name + " received " + finalDamage + " damage!");
    }

    public void receiveSpecialDamage(int damage) {

        int finalDamage =damage - spDefense;

        if (finalDamage < 1) {
            finalDamage = 0;
        }

        currentHp -= finalDamage;

        if (currentHp < 0) {
            currentHp = 0;
        }

        System.out.println(name + " received " + finalDamage + " special damage!" );
    }

    public void showMoves() {

        System.out.println("\n===== MOVES =====");

        for (int i = 0; i < moves.size(); i++) {

            Move move = moves.get(i);

            System.out.println(i + " - " + move.getName() + " | " + move.getType() + " | " + move.getCategory());
        }
    }

    public void showStatus() {

        System.out.println("\n====================");
        System.out.println(id + " - " + name);
        System.out.println("Type: " + type1 + (type2 != null? "/" + type2: ""));
        System.out.println("HP: " + currentHp + "/" + maxHp);
        System.out.println("Attack: " + attack);
        System.out.println("Defense: " + defense);
        System.out.println("SpAttack: " + spAttack);
        System.out.println("SpDefense: " + spDefense);
        System.out.println("Speed: " + speed);
        System.out.println("====================");
    }

    public void showSummary() {

        System.out.printf("%3d - %-12s %-10s | HP:%3d ATK:%3d DEF:%3d SPATK:%3d SPDEF:%3d SPD:%3d | TOTAL:%3d%n", id, name,
                type1 + (type2 != null ? "/" + type2 : ""), currentHp, attack, defense, spAttack, spDefense, speed, total);
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }

    public void healFull() {
        this.currentHp = maxHp;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MoveType getType1() {
        return type1;
    }

    public MoveType getType2() {
        return type2;
    }

    public int getHp() {
        return currentHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpAttack() {
        return spAttack;
    }

    public int getSpDefense() {
        return spDefense;
    }

    public int getSpeed() {
        return speed;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }
}
