package edu.lmu.cs.xlg.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IdGeneratorTest {

    @Test
    public void idsGenerateProperly() {
        IdGenerator generator = new IdGenerator();
        assertThat(generator.id("f"), is("f0"));
        assertThat(generator.id("g"), is("g0"));
        assertThat(generator.id("dog"), is("dog0"));
        assertThat(generator.id("g"), is("g1"));
        assertThat(generator.id("g"), is("g2"));
        assertThat(generator.id("f"), is("f1"));
    }

    @Test
    public void countsAreNotSharedBetweenGenerators() {
        IdGenerator generator = new IdGenerator();
        assertThat(generator.id("f"), is("f0"));
        IdGenerator anotherGenerator = new IdGenerator();
        assertThat(anotherGenerator.id("f"), is("f0"));
        assertThat(anotherGenerator.id("f"), is("f1"));
        assertThat(anotherGenerator.id("f"), is("f2"));
        assertThat(generator.id("f"), is("f1"));
    }
}
