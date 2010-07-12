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
package org.jesperdj.scalaray.reflection

import scala.collection.immutable.IndexedSeq

import org.jesperdj.scalaray.spectrum._
import org.jesperdj.scalaray.vecmath._

// TODO: Not yet implemented

// Bidirectional Scattering Distribution Function
final class BSDF (bxdfs: IndexedSeq[BxDF]) extends ((Vector, Vector) => Spectrum) {
	// TODO: Description
	def apply(wi: Vector, wo: Vector): Spectrum = Spectrum.Unit // TODO

	// TODO: Description. Returns wi and pdf
	def sample(wo: Vector, u1: Double, u2: Double, u3: Double): (Vector, Double) =
		throw new UnsupportedOperationException("Not yet implemented") // TODO

}