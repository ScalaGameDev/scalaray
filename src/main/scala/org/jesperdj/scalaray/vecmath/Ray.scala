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
package org.jesperdj.scalaray.vecmath

import org.jesperdj.scalaray.common.Interval

// Ray (pbrt 2.5)
sealed class Ray (val origin: Point, val direction: Vector, val range: Interval = new Interval(0.0, Double.PositiveInfinity), val depth: Int = 0) {
  // Check if distance t is in the valid range of this ray
  def isInRange(t: Double) = range.inside(t)

  // Get a point along the ray
  def apply(t: Double) = origin + direction * t
}

object Ray {
  // Create a ray
  def apply(origin: Point, direction: Vector, range: Interval, depth: Int = 0) =
    new Ray(origin, direction, range, depth)
}
