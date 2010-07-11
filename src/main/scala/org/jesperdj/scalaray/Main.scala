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
package org.jesperdj.scalaray

import java.io.File
import javax.imageio.ImageIO

import scala.actors.Futures._
import scala.collection.immutable.Traversable

import org.jesperdj.scalaray.camera._
import org.jesperdj.scalaray.filter._
import org.jesperdj.scalaray.integrator._
import org.jesperdj.scalaray.lightsource._
import org.jesperdj.scalaray.material._
import org.jesperdj.scalaray.raster._
import org.jesperdj.scalaray.sampler._
import org.jesperdj.scalaray.scene._
import org.jesperdj.scalaray.shape._
import org.jesperdj.scalaray.spectrum._
import org.jesperdj.scalaray.vecmath._

object Main {
	def main(args: Array[String]) {
		println("ScalaRay - Ray tracer based on pbrt (see http://pbrt.org) written in Scala 2.8")
		println("Copyright (C) 2009, 2010  Jesper de Jong")

		println()
		println("Setup...")
		val scene = createScene()

		val rect = new Rectangle(0, 0, 799, 599)
		val raster = new Raster(rect.width, rect.height)

		val camera: Camera = new PerspectiveCamera(Transform.translate(0.0, 0.75, 0.0), π / 4.0, rect.width, rect.height)
		val surfaceIntegrator: SurfaceIntegrator = DirectLightingSurfaceIntegrator(scene)
		val volumeIntegrator: VolumeIntegrator = VacuumVolumeIntegrator
		val sampler1: Sampler = new StratifiedSampler(new Rectangle(0, 0, 399, 599), 2, 2, surfaceIntegrator.sampleSpecs ++ volumeIntegrator.sampleSpecs)
		val sampler2: Sampler = new StratifiedSampler(new Rectangle(400, 0, 799, 599), 2, 2, surfaceIntegrator.sampleSpecs ++ volumeIntegrator.sampleSpecs)
		val filter: Filter = new MitchellFilter

		println("- Camera: " + camera)
		println("- Surface integrator: " + surfaceIntegrator)
		println("- Volume integrator: " + volumeIntegrator)
		println("- Sampler1: " + sampler1)
		println("- Sampler2: " + sampler2)
		println("- Filter: " + filter)
//		println("- Scene: " + scene)

		def render(sampler: Sampler): Unit = {
			def radiance(ray: Ray, sample: Sample): Spectrum =
				surfaceIntegrator.radiance(ray, sample) * volumeIntegrator.transmittance(ray, sample) + volumeIntegrator.radiance(ray, sample)

			for (sample <- sampler.samples) {
				// Generate camera ray and compute radiance along the ray at the image plane
				val rad = radiance(camera.generateRay(sample), sample)

				// Determine the raster extent of the sample
				val ix = sample.imageX - 0.5
				val iy = sample.imageY - 0.5
				val minX = math.max(math.ceil(ix - filter.extentX).toInt, rect.left)
				val maxX = math.min(math.floor(ix + filter.extentX).toInt, rect.right)
				val minY = math.max(math.ceil(iy - filter.extentY).toInt, rect.top)
				val maxY = math.min(math.floor(iy + filter.extentY).toInt, rect.bottom)

				// Add radiance to relevant pixels in the raster, weighted by reconstruction filter
				for (y <- minY to maxY; x <- minX to maxX) raster(x, y).add(rad, filter(x - ix, y - iy))
			}
		}

		println()
		println("Rendering...")
		val timer = new Timer("Total rendering time")
		timer.time {
			val f1 = future { render(sampler1) }
			val f2 = future { render(sampler2) }
			f1(); f2()
		}
		println(timer.toString)

		ImageIO.write(raster.toImage, "png", new File("output.png"))

		println()
		println("Finished")
	}

	def createScene(): Scene = {
//		val s1 = new Sphere(0.75, -1.0, 1.0, π * 4.0 / 3.0)
//		val p1 = new TransformedPrimitive(new GeometricPrimitive(s1, new Material), Transform.translate(0.0, 0.75, 4.0) * Transform.rotateY(π / 4.0) * Transform.rotateZ(π / 2.0))
		val p1 = new TransformedPrimitive(createCube(0.5, new Material), Transform.translate(0.0, 0.75, 4.0) * Transform.rotateY(-π / 6.0) * Transform.rotateX(-π / 6.0))

		val s2 = new Disk(3.0)
		val p2 = new TransformedPrimitive(new GeometricPrimitive(s2, new Material), Transform.translate(0.0, 0.0, 4.0) * Transform.rotateX(-π / 2.0))

//		val l1 = new DirectionalLightSource(new Vector(-0.5, -1.25, 4.0), new Spectrum(0.4, 0.4, 0.4))
		val l1 = new PointLightSource(new Point(0.5, 2.0, 0.0), new Spectrum(5.0, 5.0, 5.0))

		val s3 = new Disk(1.5)
		val t3 = Transform.translate(-0.3, 5.0, 3.5) * Transform.rotateX(π / 2.0)
		val l2 = new AreaLightSource(s3, t3, new Spectrum(0.1, 0.1, 0.1), 4, 4)
		val p3 = new TransformedPrimitive(new GeometricPrimitive(l2, new Material), t3)

		new Scene(new CompositePrimitive(p1, p2, p3), Traversable(l1, l2))
	}

	def createCube(size: Double, material: Material): Primitive = {
		import scala.collection.immutable.IndexedSeq

		// TODO: Properly generate normals and u, v surface coordinates for vertices

		// Generate vertices
		val v: IndexedSeq[Vertex] = for (i <- 0 to 7) yield
			new Vertex(Point(if ((i & 1) != 0) size else -size, if ((i & 2) != 0) size else -size, if ((i & 4) != 0) size else -size), Normal.Zero, 0.0, 0.0)

		// Front
		val ft1 = new GeometricPrimitive(new Triangle(v(0), v(2), v(3)), material)
		val ft2 = new GeometricPrimitive(new Triangle(v(3), v(1), v(0)), material)
		val f = new CompositePrimitive(ft1, ft2)

		// Back
		val bkt1 = new GeometricPrimitive(new Triangle(v(5), v(7), v(6)), material)
		val bkt2 = new GeometricPrimitive(new Triangle(v(6), v(4), v(5)), material)
		val bk = new CompositePrimitive(bkt1, bkt2)

		// Left
		val lt1 = new GeometricPrimitive(new Triangle(v(4), v(6), v(2)), material)
		val lt2 = new GeometricPrimitive(new Triangle(v(2), v(0), v(4)), material)
		val l = new CompositePrimitive(lt1, lt2)

		// Right
		val rt1 = new GeometricPrimitive(new Triangle(v(1), v(3), v(7)), material)
		val rt2 = new GeometricPrimitive(new Triangle(v(7), v(5), v(1)), material)
		val r = new CompositePrimitive(rt1, rt2)

		// Bottom
		val btt1 = new GeometricPrimitive(new Triangle(v(4), v(0), v(1)), material)
		val btt2 = new GeometricPrimitive(new Triangle(v(1), v(5), v(4)), material)
		val bt = new CompositePrimitive(btt1, btt2)

		// Top
		val tt1 = new GeometricPrimitive(new Triangle(v(7), v(3), v(2)), material)
		val tt2 = new GeometricPrimitive(new Triangle(v(2), v(6), v(7)), material)
		val t = new CompositePrimitive(tt1, tt2)

		new CompositePrimitive(f, bk, l, r, bt, t)
	}
}
