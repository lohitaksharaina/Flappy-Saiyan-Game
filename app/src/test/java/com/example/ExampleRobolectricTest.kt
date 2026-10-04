package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CharacterType
import com.example.model.SaiyanForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Flappy Saiyan Game", appName)
    }

    @Test
    fun testGokuTransformationThresholds() {
        assertEquals(SaiyanForm.BASE, SaiyanForm.getFormForScore(0, CharacterType.GOKU))
        assertEquals(SaiyanForm.BASE, SaiyanForm.getFormForScore(4, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN, SaiyanForm.getFormForScore(5, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN, SaiyanForm.getFormForScore(9, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_2, SaiyanForm.getFormForScore(10, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_2, SaiyanForm.getFormForScore(19, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_3, SaiyanForm.getFormForScore(20, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_3, SaiyanForm.getFormForScore(24, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_GOD, SaiyanForm.getFormForScore(25, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_GOD, SaiyanForm.getFormForScore(34, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_BLUE, SaiyanForm.getFormForScore(35, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_BLUE, SaiyanForm.getFormForScore(39, CharacterType.GOKU))
        assertEquals(SaiyanForm.ULTRA_INSTINCT, SaiyanForm.getFormForScore(40, CharacterType.GOKU))
        assertEquals(SaiyanForm.ULTRA_INSTINCT, SaiyanForm.getFormForScore(44, CharacterType.GOKU))
        assertEquals(SaiyanForm.MASTERED_ULTRA_INSTINCT, SaiyanForm.getFormForScore(45, CharacterType.GOKU))
        assertEquals(SaiyanForm.MASTERED_ULTRA_INSTINCT, SaiyanForm.getFormForScore(49, CharacterType.GOKU))
        assertEquals(SaiyanForm.BLACK_OOZARU, SaiyanForm.getFormForScore(50, CharacterType.GOKU))
        assertEquals(SaiyanForm.GOLDEN_OOZARU, SaiyanForm.getFormForScore(55, CharacterType.GOKU))
        assertEquals(SaiyanForm.SUPER_SAIYAN_4, SaiyanForm.getFormForScore(65, CharacterType.GOKU))
        assertEquals(SaiyanForm.WHITE_OOZARU, SaiyanForm.getFormForScore(70, CharacterType.GOKU))
        assertEquals(SaiyanForm.ULTRA_XENO, SaiyanForm.getFormForScore(80, CharacterType.GOKU))
        assertEquals(SaiyanForm.ULTRA_XENO, SaiyanForm.getFormForScore(100, CharacterType.GOKU))
    }

    @Test
    fun testVegetaTransformationThresholds() {
        assertEquals(SaiyanForm.BASE, SaiyanForm.getFormForScore(0, CharacterType.VEGETA))
        assertEquals(SaiyanForm.SUPER_SAIYAN, SaiyanForm.getFormForScore(5, CharacterType.VEGETA))
        assertEquals(SaiyanForm.SUPER_SAIYAN_2, SaiyanForm.getFormForScore(10, CharacterType.VEGETA))
        assertEquals(SaiyanForm.SUPER_SAIYAN_3, SaiyanForm.getFormForScore(20, CharacterType.VEGETA))
        assertEquals(SaiyanForm.SUPER_SAIYAN_GOD, SaiyanForm.getFormForScore(25, CharacterType.VEGETA))
        assertEquals(SaiyanForm.SUPER_SAIYAN_BLUE, SaiyanForm.getFormForScore(35, CharacterType.VEGETA))
        assertEquals(SaiyanForm.ULTRA_EGO, SaiyanForm.getFormForScore(40, CharacterType.VEGETA))
        assertEquals(SaiyanForm.ULTRA_EGO, SaiyanForm.getFormForScore(44, CharacterType.VEGETA))
        assertEquals(SaiyanForm.MASTERED_ULTRA_EGO, SaiyanForm.getFormForScore(45, CharacterType.VEGETA))
        assertEquals(SaiyanForm.MASTERED_ULTRA_EGO, SaiyanForm.getFormForScore(49, CharacterType.VEGETA))
        assertEquals(SaiyanForm.BLACK_OOZARU, SaiyanForm.getFormForScore(50, CharacterType.VEGETA))
        assertEquals(SaiyanForm.GOLDEN_OOZARU, SaiyanForm.getFormForScore(55, CharacterType.VEGETA))
        assertEquals(SaiyanForm.SUPER_SAIYAN_4, SaiyanForm.getFormForScore(65, CharacterType.VEGETA))
        assertEquals(SaiyanForm.WHITE_OOZARU, SaiyanForm.getFormForScore(70, CharacterType.VEGETA))
        assertEquals(SaiyanForm.ULTRA_XENO, SaiyanForm.getFormForScore(80, CharacterType.VEGETA))
    }

    @Test
    fun testFormAttributes() {
        assertTrue(SaiyanForm.SUPER_SAIYAN_2.hasLightning)
        assertTrue(SaiyanForm.SUPER_SAIYAN_3.hasLightning)
        assertTrue(SaiyanForm.MASTERED_ULTRA_INSTINCT.hasLightning)
        assertTrue(SaiyanForm.MASTERED_ULTRA_EGO.hasLightning)
        assertTrue(SaiyanForm.SUPER_SAIYAN_GOD.isGodly)
        assertTrue(SaiyanForm.SUPER_SAIYAN_BLUE.isGodly)
        assertTrue(SaiyanForm.ULTRA_INSTINCT.isGodly)
        assertTrue(SaiyanForm.ULTRA_EGO.isGodly)
        assertTrue(SaiyanForm.ULTRA_XENO.isGodly)
        assertTrue(SaiyanForm.BLACK_OOZARU.isOozaru)
        assertTrue(SaiyanForm.GOLDEN_OOZARU.isOozaru)
        assertTrue(SaiyanForm.WHITE_OOZARU.isOozaru)
        org.junit.Assert.assertFalse(SaiyanForm.SUPER_SAIYAN_4.isOozaru)
        org.junit.Assert.assertFalse(SaiyanForm.ULTRA_XENO.isOozaru)
    }

    @Test
    fun testGameModes() {
        val passThrough = com.example.game.GameMode.PASS_THROUGH
        val classic = com.example.game.GameMode.CLASSIC
        assertEquals("PASS-THROUGH MODE", passThrough.title)
        assertEquals("CLASSIC MODE", classic.title)
    }
}
