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
package org.jesperdj.scalaray.lightsource

import org.jesperdj.scalaray.scene.Scene
import org.jesperdj.scalaray.spectrum.Spectrum
import org.jesperdj.scalaray.util._
import org.jesperdj.scalaray.vecmath._

// Spot light source (pbrt 12.2.1)
final class SpotLightSource (position: Point, direction: Vector, widthAngle: Float, falloffAngle: Float, intensity: Spectrum) extends DeltaLightSource {
	require(direction.length > 0.999f && direction.length < 1.001f, "SpotLightSource requires that the direction vector is normalized")
	require(widthAngle > falloffAngle, "SpotLightSource requires that the width angle must be greater than the falloff angle")

	private val cosWidth = math.cos(widthAngle).toFloat
	private val cosFalloff = math.cos(falloffAngle).toFloat

	// Create a spot light source using a light-to-world transform
	def this(lightToWorld: Transform, widthAngle: Float, falloffAngle: Float, intensity: Spectrum) =
		this(lightToWorld * Point.Origin, lightToWorld * Vector.ZAxis, widthAngle, falloffAngle, intensity)

	// Radiance of this light source at the given point
	// Returns the radiance and a ray from the light source to the given point
	def radiance(point: Point): (Spectrum, Ray) = {
		val rd = point - position

		// Compute falloff factor
		val c = direction * rd.normalize
		val falloff = if (c < cosWidth) 0.0f else if (c > cosFalloff) 1.0f else {
			val delta = (c - cosWidth) / (cosFalloff - cosWidth)
			delta * delta * delta * delta
		}

		(if (falloff > 0.0f) intensity * (falloff / rd.lengthSquared) else Spectrum.Black, Ray(position, rd, 0.0f, 1.0f))
	}

	// Total emitted power of this light source onto the scene
	def totalPower(scene: Scene): Spectrum = intensity * (2.0f * π * (1.0f - 0.5f * (cosFalloff + cosWidth)))

	override def toString = "SpotLightSource(position=%s, direction=%s, widthAngle=%g, falloffAngle=%g, intensity=%s)" format
		(position, direction, widthAngle, falloffAngle, intensity)
}
