/*
 * ScalaRay - Ray tracer based on pbrt (see http://pbrt.org) written in Scala 2.8
 * Copyright (C) 2009, 2010  Jesper de Jong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jesperdj.scalaray.integrator

import scala.collection.immutable.Traversable

import org.jesperdj.scalaray.sampler.{ Sample, SampleSpec }
import org.jesperdj.scalaray.spectrum.Spectrum
import org.jesperdj.scalaray.vecmath.Ray

// Volume integrator that does nothing
object VacuumVolumeIntegrator extends VolumeIntegrator {
	// Sample specifications for the sample patterns that this integrator needs
	val sampleSpecs: Traversable[SampleSpec] = Traversable()

	// Radiance along the ray
	def radiance(ray: Ray, sample: Sample): Spectrum = Spectrum.Black
	
	// Transmittance along the ray
	def transmittance(ray: Ray, sample: Sample): Spectrum = Spectrum.Unit

	override def toString = "VacuumVolumeIntegrator"
}
