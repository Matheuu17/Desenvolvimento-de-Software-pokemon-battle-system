package battle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import pokemon.DefaultPokemon;
import pokemon.Pokemon;
import moves.MoveType;
import trainer.Trainer;

public class BattleSystemTest {

    private Pokemon fastPokemon;
    private Pokemon slowPokemon;
    private Pokemon equalPokemon1;
    private Pokemon equalPokemon2;
    private BattleSystem battleSystem;

    @BeforeEach
    public void setUp() {
        // Inicialización correcta usando el constructor original de 11 parámetros del
        // profesor
        // id, name, type1, type2, total, hp, attack, defense, spAttack, spDefense,
        // speed
        fastPokemon = new DefaultPokemon(1, "Pikachu", MoveType.NORMAL, null, 320, 100, 55, 40, 50, 50, 130);
        slowPokemon = new DefaultPokemon(2, "Snorlax", MoveType.NORMAL, null, 540, 160, 110, 65, 65, 110, 30);
        equalPokemon1 = new DefaultPokemon(3, "Bulbasaur", MoveType.NORMAL, null, 318, 100, 49, 49, 65, 65, 45);
        equalPokemon2 = new DefaultPokemon(4, "Squirtle", MoveType.NORMAL, null, 314, 100, 48, 65, 50, 64, 45);

        battleSystem = new BattleSystem();
    }

    // =========================================================================
    // REGLA 6: ESTADO INICIAL VÁLIDO
    // =========================================================================

    @Test
    void crearPokemonConHpInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultPokemon(1, "Pikachu", MoveType.NORMAL, null, 320,
                0, // HP inválido (<= 0)
                55, 40, 50, 50, 130));
    }
    // =========================================================================
    // REGLA 1: ORDEN DE TURNOS (nuevas)
    // =========================================================================

    @Test
    void testRegla1_SlowPokemonNOAtacaPrimero() {
        Pokemon primero = battleSystem.determinarPrimerTurno(fastPokemon, slowPokemon);
        assertNotEquals("Snorlax", primero.getName(),
                "QA ERROR: El Pokémon más lento NO debe liderar el turno.");
    }

    @Test
    void testRegla1_ResultadoNoEsNulo() {
        Pokemon primero = battleSystem.determinarPrimerTurno(fastPokemon, slowPokemon);
        assertNotNull(primero,
                "QA ERROR: determinarPrimerTurno no debe retornar null.");
    }

    @Test
    void testRegla1_MismaVelocidadRetornaUnoDeLosDosPokemon() {
        Pokemon primero = battleSystem.determinarPrimerTurno(equalPokemon1, equalPokemon2);
        boolean esUnoDeLosDos = primero.getName().equals("Bulbasaur")
                || primero.getName().equals("Squirtle");
        assertTrue(esUnoDeLosDos,
                "QA ERROR: Con velocidades iguales debe retornar uno de los dos Pokémon.");
    }

    // =========================================================================
    // REGLA 3: EFECTIVIDAD POR TIPO (nuevas)
    // =========================================================================

    @Test
    void testRegla3_SuperEfectivoEsMayorQueNeutro() {
        double superEfectivo = battleSystem.obtenerFactorEfectividad("AGUA", "FUEGO");
        double neutro = battleSystem.obtenerFactorEfectividad("NORMAL", "AGUA");
        assertTrue(superEfectivo > neutro,
                "QA ERROR: Súper efectivo debe ser mayor que neutro.");
    }

    @Test
    void testRegla3_PocoEfectivoEsMenorQueNeutro() {
        double pocoEfectivo = battleSystem.obtenerFactorEfectividad("FUEGO", "AGUA");
        double neutro = battleSystem.obtenerFactorEfectividad("NORMAL", "AGUA");
        assertTrue(pocoEfectivo < neutro,
                "QA ERROR: Poco efectivo debe ser menor que neutro.");
    }

    @Test
    void testRegla3_FactorNuncaEsNegativo() {
        double factor = battleSystem.obtenerFactorEfectividad("FUEGO", "AGUA");
        assertTrue(factor >= 0,
                "QA ERROR: El factor de efectividad nunca puede ser negativo.");
    }
}