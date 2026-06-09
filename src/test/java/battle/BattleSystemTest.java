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
        int danioCalculado = battleSystem.calcularDanioEmitido(slowPokemon, fastPokemon, "NORMAL");

        fastPokemon.receiveDamage(danioCalculado);

        // Aquí se hace la comparación
        boolean esValido = fastPokemon.getHp() <= hpInicial;

        assertTrue(esValido);
    }

    @Test
    public void testRegla2_VidaNuncaQuedaEnNegativo() {
        // Creamos un atacante con estadísticas masivas para simular un "CRITICO"
        Pokemon SnorlaxDios = new DefaultPokemon(2, "Snorlax", MoveType.NORMAL, null, 999, 160, 9999, 65, 65, 110, 30);

        // Ejecutamos el asalto real utilizando el sistema de batalla
        // Snorlax ataca a Pikachu
        battleSystem.ejecutarAsalto(SnorlaxDios, fastPokemon, 1);

        // El HP resultante del defensor debe ser exactamente 0, nunca negativo
        int hpFinal = fastPokemon.getHp();

        assertEquals(0, hpFinal);
    }

    @Test
    public void testRegla2_CalculoSeAplicaUnaSolaVezPorTurno() {
        // Guardamos la vida inicial
        int hpInicial = fastPokemon.getHp();

        // Llamamos a la funcion creada
        int danioSimulado = battleSystem.calcularDanioEmitido(slowPokemon, fastPokemon, "NORMAL");

        // Ejecutamos el asalto real
        battleSystem.ejecutarAsalto(slowPokemon, fastPokemon, 1);

        // Guardamos el resultado real
        int hpFinalReal = fastPokemon.getHp();

        // El HP esperado lógicamente no puede bajar de 0
        int hpEsperado = hpInicial - danioSimulado;
        if (hpEsperado < 0) {
            hpEsperado = 0;
        }

        assertEquals(hpEsperado, hpFinalReal);
    }

    // =========================================================================
    // REGLA 3: EFECTIVIDAD POR TIPO
    // =========================================================================

    @Test
    public void testRegla3_AtaquesEfectivosCausanMasDanio() {

        // LLAMAMOS A LA FUNCION CREADA
        double multiplicadorReal = battleSystem.obtenerFactorEfectividad("AGUA", "FUEGO");

        // VERIFICACIÓN: Validamos el valor exacto del juego (Súper Efectivo = 2.0)

        assertEquals(2.0, multiplicadorReal, 0.001);
    }

    @Test
    public void testRegla3_AtaquesPocoEfectivosCausanMenosDanio() {
        // LLAMAMOS A LA FUNCIÓN CREADA
        double multiplicadorReal = battleSystem.obtenerFactorEfectividad("FUEGO", "AGUA");

        // VERIFICACIÓN: Validamos el valor exacto del juego (Poco Efectivo = 0.5)
        assertEquals(0.5, multiplicadorReal, 0.001);
    }

    @Test
    public void testRegla3_AtaquesNeutrosMantienenDanioBase() {
        // LLAMAMOS A LA FUNCIÓN CREADA
        double multiplicadorReal = battleSystem.obtenerFactorEfectividad("NORMAL", "AGUA");

        // VERIFICACIÓN: Validamos el valor exacto del juego (Neutro = 1.0)

        assertEquals(1.0, multiplicadorReal, 0.001);
    }

    // =========================================================================
    // REGLA 4: CONDICIÓN DE DERROTA
    // =========================================================================

    @Test
    public void testRegla4_CombateTerminaCuandoVidaLlegaACero() {

        // snorlax súper fuerte para debilitar a Pikachu de un golpe
        Pokemon snorlaxDios = new DefaultPokemon(2, "Snorlax", MoveType.NORMAL, null, 999, 160, 9999, 65, 65, 110, 30);

        // Ejecutamos el asalto para que el daño real debilite al rival
        battleSystem.ejecutarAsalto(snorlaxDios, fastPokemon, 1);

        // Guardamos el booleano puro fuera del assert
        boolean combateFinalizado = battleSystem.verificarCombateFinalizado(fastPokemon, snorlaxDios);

        assertTrue(combateFinalizado);
    }

    @Test
    public void testRegla4_PokemonDerrotadoNoVuelveAAtacar() {
        // Debilitamos a Pikachu usando el flujo del sistema de batalla
        Pokemon snorlaxDios = new DefaultPokemon(2, "Snorlax", MoveType.NORMAL, null, 999, 160, 9999, 65, 65, 110, 30);
        battleSystem.ejecutarAsalto(snorlaxDios, fastPokemon, 1);

        // El Pikachu debilitado intenta contraatacar en su turno
        boolean accionEjecutada = battleSystem.ejecutarAsalto(fastPokemon, snorlaxDios, 0);

        assertFalse(accionEjecutada);
    }

    @Test
    public void testRegla4_SistemaIdentificaCorrectamenteAlGanador() {
        // Creamos un Pikachu súper fuerte para noquear a Snorlax de forma legal
        Pokemon pikachuDios = new DefaultPokemon(1, "Pikachu", MoveType.NORMAL, null, 999, 100, 9999, 40, 50, 50, 130);

        // Pikachu noquea a Snorlax dentro del sistema de combate
        battleSystem.ejecutarAsalto(pikachuDios, slowPokemon, 0);

        // Obtenemos las referencias reales para comparar
        Pokemon ganador = battleSystem.obtenerGanador(pikachuDios, slowPokemon);
        String nombreGanadorReal = ganador.getName();

        assertEquals("Pikachu", nombreGanadorReal);
    }

    // =========================================================================
    // REGLA 5: INMUTABILIDAD DE ATRIBUTOS
    // =========================================================================

    @Test
    public void testRegla5_AtributosNoCambianSinCausaJustificada() {
        // Capturamos las "fotografías" iniciales de los atributos estáticos
        MoveType tipoOriginal = fastPokemon.getType1();
        int velocidadOriginal = fastPokemon.getSpeed();

        // Ejecutamos el asalto real utilizando el sistema de batalla
        battleSystem.ejecutarAsalto(slowPokemon, fastPokemon, 0);

        // Guardamos los estados reales resultantes en variables locales
        MoveType tipoFinalReal = fastPokemon.getType1();
        int velocidadFinalReal = fastPokemon.getSpeed();

        assertEquals(tipoOriginal, tipoFinalReal);
        assertEquals(velocidadOriginal, velocidadFinalReal);
    }

    @Test
    public void testRegla5_NoExistenEfectosColateralesInesperados() {
        // Guardamos el ataque original del atacante antes del golpe
        int ataqueOriginalSnorlax = slowPokemon.getAttack();

        // Ejecutamos el asalto en el sistema de batalla
        battleSystem.ejecutarAsalto(slowPokemon, fastPokemon, 0);

        // Guardamos el ataque real final después del impacto
        int ataqueFinalRealSnorlax = slowPokemon.getAttack();

        assertEquals(ataqueOriginalSnorlax, ataqueFinalRealSnorlax);
    }
}