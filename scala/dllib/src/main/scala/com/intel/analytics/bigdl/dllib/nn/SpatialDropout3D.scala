/*
 * Copyright 2016 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.analytics.bigdl.dllib.nn

import com.intel.analytics.bigdl.dllib.nn.abstractnn.{DataFormat, IdentityOutputShape, TensorModule}
import com.intel.analytics.bigdl.dllib.tensor.Tensor
import com.intel.analytics.bigdl.dllib.tensor.TensorNumericMath.TensorNumeric
import com.intel.analytics.bigdl.dllib.utils.Log4Error

import scala.reflect.ClassTag

/**
 *
 *  This version performs the same function as Dropout, however it drops
 *  entire 3D feature maps instead of individual elements. If adjacent voxels
 *  within feature maps are strongly correlated (as is normally the case in
 *  early convolution layers) then regular dropout will not regularize the
 *  activations and will otherwise just result in an effective learning rate
 *  decrease. In this case, SpatialDropout3D will help promote independence
 *  between feature maps and should be used instead.
 *
 * @param initP the probability p
 * @param format  'NCHW' or 'NHWC'.
            In 'NCHW' mode, the channels dimension (the depth)
            is at index 1, in 'NHWC' mode is it at index 4.
 * @tparam T The numeric type in the criterion, usually which are [[Float]] or [[Double]]
 */
@SerialVersionUID(- 4636332259181125718L)
class SpatialDropout3D[T: ClassTag](
  val initP: Double = 0.5,
  val format: DataFormat = DataFormat.NCHW)(
  implicit ev: TensorNumeric[T]) extends TensorModule[T] {
  var p = initP
  var noise = Tensor[T]()

  override def updateOutput(input: Tensor[T]): Tensor[T] = {
    this.output.resizeAs(input).copy(input)

    if (train) {
      val inputSize = input.size()
      if (input.dim() == 4) {
        if (format == DataFormat.NCHW) {
          noise.resize(Array(inputSize(0), 1, 1, 1))
        } else if (format == DataFormat.NHWC) {
          noise.resize(Array(1, 1, 1, inputSize(3)))
        } else {
          Log4Error.invalidInputError(false, s"SpatialDropout3D:" +
            " DataFormat: " + format + " is not supported", "only support NHWC and NCHW")
        }
      } else if (input.dim() == 5) {
        if (format == DataFormat.NCHW) {
          noise.resize(Array(inputSize(0), inputSize(1), 1, 1, 1))
        } else if (format == DataFormat.NHWC)  {
          noise.resize(Array(inputSize(0), 1, 1, 1, inputSize(4)))
        } else {
          Log4Error.invalidInputError(false, s"SpatialDropout3D:" +
            " DataFormat: " + format + " is not supported", "only support NHWC and NCHW")
        }
      } else {
        Log4Error.invalidInputError(false, s"unsupported input dim ${input.dim()}",
          "SpatialDropout3D: Input must be 4D or 5D")
      }
      noise.bernoulli(1 - p)
      output.cmul(noise.expandAs(input))
    } else {
      this.output.mul(ev.fromType[Double](1 - p))
    }
  }

  override def updateGradInput(input: Tensor[T], gradOutput: Tensor[T]): Tensor[T] = {
    if (train) {
      gradInput.resizeAs(gradOutput).copy(gradOutput)
      gradInput.cmul(noise.expandAs(input))
    } else {
      Log4Error.invalidOperationError(false,
        "SpatialDropout3D: backprop only defined while training")
    }

    this.gradInput
  }

  override def clearState(): this.type = {
    super.clearState()
    noise.set()
    this
  }


  override def toString(): String = {
    s"${getPrintName}($p)"
  }
}

object SpatialDropout3D {
  def apply[T: ClassTag](
    initP: Double = 0.5,
    format: DataFormat = DataFormat.NCHW)(implicit ev: TensorNumeric[T]): SpatialDropout3D[T] = {
    new SpatialDropout3D[T](initP, format)
  }
}
