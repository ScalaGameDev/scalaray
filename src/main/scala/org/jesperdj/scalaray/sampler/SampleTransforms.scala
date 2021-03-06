/*
 * ScalaRay - Ray tracer based on pbrt (see http://pbrt.org) written in Scala
 * Copyright (C) 2009, 2010, 2011  Jesper de Jong
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
package org.jesperdj.scalaray.sampler

import org.jesperdj.scalaray.common._
import org.jesperdj.scalaray.vecmath._

// Transforming samples between distributions (pbrt 13.5, 13.6)
object SampleTransforms {
  // Sample a point on the unit hemisphere by uniform mapping (pbrt 13.6.1)
  def uniformSampleHemisphere(u1: Double, u2: Double): Point = {
    val z = u1
    val r = math.sqrt(math.max(0.0, 1.0 - z * z))
    val phi = 2.0 * π * u2

    new Point(r * math.cos(phi), r * math.sin(phi), z)
  }

  // Probability distribution function for uniform sampling on the unit hemisphere (pbrt 13.6.1)
  val uniformHemispherePdf: Double = 1.0 / (2.0 * π)

  // Sample a point on the unit sphere by uniform mapping (pbrt 13.6.1)
  def uniformSampleSphere(u1: Double, u2: Double): Point = {
    val z = 1.0 - 2.0 * u1
    val r = math.sqrt(math.max(0.0, 1.0 - z * z))
    val phi = 2.0 * π * u2

    new Point(r * math.cos(phi), r * math.sin(phi), z)
  }

  // Probability distribution function for uniform sampling on the unit sphere (pbrt 13.6.1)
  val uniformSpherePdf: Double = 1.0 / (4.0 * π)

  // Sample a direction from a cone around the z-axis (pbrt 14.6.2)
  def uniformSampleCone(u1: Double, u2: Double, cosThetaMax: Double): Vector = {
    val cosTheta = (1.0 - u1) + u1 * cosThetaMax
    val sinTheta = math.sqrt(1.0 - cosTheta * cosTheta)
    val phi = 2.0 * π * u2

    new Vector(math.cos(phi) * sinTheta, math.sin(phi) * sinTheta, cosTheta)
  }

  // Sample a direction from a cone around the z-axis of the coordinate system determined by the given vectors (pbrt 14.6.2)
  def uniformSampleCone(u1: Double, u2: Double, cosThetaMax: Double, x: Vector, y: Vector, z: Vector): Vector =
    throw new UnsupportedOperationException("Not yet implemented")

  // Probability distribution function for sampling from a cone (pbrt 14.6.2)
  def uniformConePdf(cosThetaMax: Double): Double = 1.0 / (2.0 * π * (1.0 - cosThetaMax))

  // Sample a point on the unit disk by uniform mapping (pbrt 13.6.2)
  def uniformSampleDisk(u1: Double, u2: Double): (Double, Double) = {
    val r = math.sqrt(u1)
    val theta = 2.0 * π * u2

    (r * math.cos(theta), r * math.sin(theta))
  }

  // Sample a point on the unit disk using Shirley's concentric mapping method (pbrt 13.6.2)
  def concentricSampleDisk(u1: Double, u2: Double): (Double, Double) = {
    val sx = 2.0 * u1 - 1.0
    val sy = 2.0 * u2 - 1.0

    // Handle degeneracy at the origin
    if (sx == 0.0 && sy == 0.0) (0.0, 0.0) else {
      var r = 0.0
      var theta = 0.0

      if (sx >= -sy) {
        if (sx > sy) {
          // Handle first region of disk
          r = sx; theta = if (sy > 0.0) sy / r else (8.0 + sy / r)
        }
        else {
          // Handle second region of disk
          r = sy; theta = 2.0 - sx / r
        }
      }
      else {
        if (sx <= sy) {
          // Handle third region of disk
          r = -sx; theta = 4.0 - sy / r
        }
        else {
          // Handle fourth region of disk
          r = -sy; theta = 6.0 + sx / r
        }
      }

      theta *= π / 4.0

      (r * math.cos(theta), r * math.sin(theta))
    }
  }

  // Sample a point on the unit hemisphere using Malley's cosine mapping method (pbrt 13.6.3)
  def cosineSampleHemisphere(u1: Double, u2: Double): Point = {
    val (x, y) = concentricSampleDisk(u1, u2)
    new Point(x, y, math.sqrt(math.max(0.0, 1.0 - x * x - y * y)))
  }

  // Probability distribution function that matches cosineSampleHemisphere
  def cosineHemispherePdf(wi: Vector): Double = wi.z.abs / π

  // Sample a point on a triangle by uniform mapping (pbrt 13.6.4), returns barycentric coordinates
  def uniformSampleTriangle(u1: Double, u2: Double): (Double, Double) = {
    val su1 = math.sqrt(u1)
    (1.0 - su1, u2 * su1)
  }
}
