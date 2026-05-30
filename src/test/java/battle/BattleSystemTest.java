package battle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import pokemon.DefaultPokemon;
import pokemon.Pokemon;
import moves.MoveType;

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
    // REGLA 1: ORDEN DE TURNOS
    // =========================================================================

    @Test
    public void testRegla1_MayorVelocidadAtacaPrimero() {
        Pokemon primero = battleSystem.determinarPrimerTurno(fastPokemon, slowPokemon);
        assertEquals("Pikachu", primero.getName(), "QA ERROR: El Pokémon más rápido debe liderar el turno.");
    }

    @Test
    public void testRegla1_MismaVelocidadOrdenConsistente() {
        Pokemon primeroCamada1 = battleSystem.determinarPrimerTurno(equalPokemon1, equalPokemon2);
        Pokemon primeroCamada2 = battleSystem.determinarPrimerTurno(equalPokemon1, equalPokemon2);

        assertEquals(primeroCamada1.getName(), primeroCamada2.getName(),
                "QA ERROR: El orden con velocidades iguales debe ser consistente.");
    }

    // =========================================================================
    // REGLA 2: CÁLCULO DE DAÑO
    // =========================================================================

    @Test
    public void testRegla2_DanioNuncaIncrementaVida() {
        int hpInicial = fastPokemon.getHp();
        // Cambiado "NORMAL" por el índice numérico 0 para concordar con la firma de
        // producción
        // Línea 61 corregida con el String correcto:
        int danioCalculado = battleSystem.calcularDanioEmitido(slowPokemon, fastPokemon, "NORMAL");

        fastPokemon.receiveDamage(danioCalculado);
        assertTrue(fastPokemon.getHp() <= hpInicial, "QA ERROR: El daño jamás debe curar o subir el HP.");
    }

    @Test
    public void testRegla2_VidaNuncaQuedaEnNegativo() {
        fastPokemon.receiveDamage(9999); // Forzamos daño masivo directo
        assertEquals(0, fastPokemon.getHp(),
                "QA ERROR: El HP resultante de un exceso de daño debe ser estrictamente 0.");
    }

    @Test
    public void testRegla2_CalculoSeAplicaUnaSolaVezPorTurno() {
        int hpInicial = fastPokemon.getHp();

        // CAMBIAR EL 0 POR 1 para que use un ataque real de su pool y no la defensa
        battleSystem.ejecutarAsalto(slowPokemon, fastPokemon, 1);

        // Verificamos inmutabilidad: tras recibir el impacto, el HP final debe reflejar
        // un descuento
        assertTrue(fastPokemon.getHp() < hpInicial, "QA ERROR: El daño del asalto no modificó el HP.");
    }

    // =========================================================================
    // REGLA 3: EFECTIVIDAD POR TIPO
    // =========================================================================

    @Test
    public void testRegla3_AtaquesEfectivosCausanMasDanio() {
        double multiplicador = battleSystem.obtenerFactorEfectividad("AGUA", "FUEGO");
        assertTrue(multiplicador > 1.0, "QA ERROR: Un ataque efectivo debe tener un multiplicador mayor a 1.0.");
    }

    @Test
    public void testRegla3_AtaquesPocoEfectivosCausanMenosDanio() {
        double multiplicador = battleSystem.obtenerFactorEfectividad("FUEGO", "AGUA");
        assertTrue(multiplicador < 1.0,
                "QA ERROR: Un ataque poco efectivo debe reducir el daño (multiplicador < 1.0).");
    }

    @Test
    public void testRegla3_AtaquesNeutrosMantienenDanioBase() {
        double multiplicador = battleSystem.obtenerFactorEfectividad("NORMAL", "AGUA");
        assertEquals(1.0, multiplicador, "QA ERROR: Un ataque neutro debe mantener el multiplicador en 1.0.");
    }

    // =========================================================================
    // REGLA 4: CONDICIÓN DE DERROTA
    // =========================================================================

    @Test
    public void testRegla4_CombateTerminaCuandoVidaLlegaACero() {
        fastPokemon.receiveDamage(9999); // Forzamos debilitación
        assertTrue(battleSystem.verificarCombateFinalizado(fastPokemon, slowPokemon),
                "QA ERROR: El combate debe marcarse como terminado si un Pokémon cae.");
    }

    @Test
    public void testRegla4_PokemonDerrotadoNoVuelveAAtacar() {
        fastPokemon.receiveDamage(9999); // Pikachu cae debilitado

        boolean accionEjecutada = battleSystem.ejecutarAsalto(fastPokemon, slowPokemon, 0);
        assertFalse(accionEjecutada, "QA ERROR: Un Pokémon debilitado no tiene permitido ejecutar ataques.");
    }

    @Test
    public void testRegla4_SistemaIdentificaCorrectamenteAlGanador() {
        slowPokemon.receiveDamage(9999); // Snorlax cae debilitado
        Pokemon ganador = battleSystem.obtenerGanador(fastPokemon, slowPokemon);
        assertEquals("Pikachu", ganador.getName(),
                "QA ERROR: El sistema no reconoció correctamente al Pokémon vencedor.");
    }

    // =========================================================================
    // REGLA 5: INMUTABILIDAD DE ATRIBUTOS
    // =========================================================================

    @Test
    public void testRegla5_AtributosNoCambianSinCausaJustificada() {
        MoveType tipoOriginal = fastPokemon.getType1(); // Corregido getType() por getType1()
        int velocidadOriginal = fastPokemon.getSpeed();

        battleSystem.ejecutarAsalto(slowPokemon, fastPokemon, 0);

        assertEquals(tipoOriginal, fastPokemon.getType1(),
                "QA ERROR: El tipo elemental del Pokémon cambió inesperadamente durante el combate.");
        assertEquals(velocidadOriginal, fastPokemon.getSpeed(),
                "QA ERROR: La velocidad del Pokémon se alteró sin justificación.");
    }

    @Test
    public void testRegla5_NoExistenEfectosColateralesInesperados() {
        int ataqueOriginalSnorlax = slowPokemon.getAttack();

        battleSystem.ejecutarAsalto(slowPokemon, fastPokemon, 0);

        assertEquals(ataqueOriginalSnorlax, slowPokemon.getAttack(),
                "QA ERROR: Modificar el HP de un Pokémon alteró los atributos estáticos del atacante.");
    }
}