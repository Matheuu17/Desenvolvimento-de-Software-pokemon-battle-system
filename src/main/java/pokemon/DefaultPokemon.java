package pokemon;

import moves.MoveType;

public class DefaultPokemon extends Pokemon {

    public DefaultPokemon(int id, String name, MoveType type1, MoveType type2, int total, int hp, int attack,
            int defense, int spAttack, int spDefense, int speed) {

        super(
                id,
                name,
                type1,
                type2,
                total,
                hp,
                attack,
                defense,
                spAttack,
                spDefense,
                speed);
    }
}