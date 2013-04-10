/**
* Author: Ebrahim Rajabzadeh
*
* Copyright: i-Vis Research Group, Bilkent University, 2009 - present 
*/

package ivis
{
	import flash.display.DisplayObject;
	import flash.display.Graphics;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import mx.formatters.NumberBaseRoundType;
	import mx.formatters.NumberFormatter;
	
	public class Utils
	{
		
		private static const _formatter: NumberFormatter = new NumberFormatter
		
		// static var initialization
		_formatter.precision = 0
		_formatter.rounding = NumberBaseRoundType.NEAREST
		
		public function Utils()
		{
			throw new Error("abstract class")
		}
		
		public static function formatNumber(n: Object): String
		{
			return _formatter.format(n)
		}
		
		public static function convertPoint(p: Point, src: DisplayObject, dest: DisplayObject): Point
		{
			var t: Point = src.localToGlobal(p)
			return dest.globalToLocal(t)
		}

		public static function boundingRect(points: Array): Object
		{
			var leftmost: * = null;
			var rightmost: * = null;
			var topmost: * = null;
			var bottommost: * = null;
			var top: Number = int.MAX_VALUE
			var left: Number = int.MAX_VALUE
			var right: Number = int.MIN_VALUE
			var bottom: Number = int.MIN_VALUE
			
			for each(var n:* in points)
			{
				if(n.y < top)
				{
					top = n.y
					topmost = n
				}
				if((n.y + n.height) > bottom)
				{
					bottom = n.y + n.height
					bottommost = n
				}
				if(n.x < left)
				{
					left = n.x
					leftmost = n
				}
				if((n.x + n.width) > right)
				{
					right = n.x + n.width
					rightmost = n
				}
			}
			
			return { top: top, left: left, right: right, bottom: bottom,
				topmost: topmost, leftmost: leftmost, rightmost: rightmost, bottommost: bottommost,
				width: Number(right - left), height: Number(bottom - top)}
		}
		
		public static function pointInBounds(x: Number, y: Number, b: Object): Boolean
		{
			return y > b.top && y < b.bottom && x < b.right && x > b.left
		}
		
		public static function copyArray(src: Array, dest: Array): void
		{
			for(var i: int = 0; i < src.length; ++i)
				dest[i] = src[i]
		}
		
		public static function cloneArray(a: Array): Array
		{
			var r: Array = new Array(a.length)
			
			for(var i: int = 0; i < a.length; ++i)
				r[i] = a[i]
				
			return r
		}
		
		public static function merge(a: Array, b: Array): Array
		{
			var res: Array = new Array(a.length + b.length)
			var i: int = 0
			
			for(; i < a.length; ++i)
				res[i] = a[i]

			for(; i < res.length; ++i)
				res[i] = b[i - a.length]
				
								
			return res
		}

		public static function colorToString(color: uint): String
		{
			var res: String = color.toString(16)
			var i: int = res.length
			
			if(i > 6)
				res = res.substr(-6, 6)
			
			while(res.length < 6)
				res = "0" + res
				
			return res
		}
		
		public static function intToRgb(color: uint): Array
		{
			var r: uint = (color & 0x00ff0000) >> 16;
			var g: uint = (color & 0x0000ff00) >> 8;
			var b: uint = (color & 0x000000ff);
			
			return [r, g, b];
		}

		public static function rgbToInt(rgb: Array): uint
		{
			return (0xffff0000 & (rgb[0] << 16)) | (0xff00ff00 & (rgb[1] << 8)) | (0xff0000ff & rgb[2]) 
		}

		public static function colorFromString(s: String): uint
		{
			var re:RegExp = /\s+/;
  			var cs:Array = s.split(re);
  			
  			return cs.length < 3 ? 0 :
  				((uint(cs[0]) + (uint(cs[1]) << 8) + (uint(cs[2]) << 16))) || 0xff000000;
		}
		
		public static function brighter(color: uint, b: int = 10): uint
		{
			var comps: Array = intToRgb(color)
			
			b = Math.min(b, 255  - comps[0], 255  - comps[1], 255  - comps[2]) 
			b = Math.max(b, 0)
			
			comps[0] += b
			comps[1] += b
			comps[2] += b

			return rgbToInt(comps)
		}
		
		public static function drawDashedLine(graphics: Graphics, x1:Number, y1:Number, x2:Number, y2:Number, dashlen:Number, gaplen:Number): void 
		{
			
			var dx: Number = x2 - x1;
			var dy: Number = y2 - y1;
			var m: Number = Math.atan2(dy, dx);
			var ddx: Number = dashlen * Math.cos(m);
			var ddy: Number = dashlen * Math.sin(m);
			var gdx: Number = gaplen * Math.cos(m);
			var gdy: Number = gaplen * Math.sin(m);
			
			var x: Number = x1;
			var y: Number = y1;
			var i: int = Math.sqrt(dx*dx + dy*dy) / (dashlen + gaplen);
			while(i >= 0) {
				graphics.moveTo(x, y);
				x += ddx;
				y += ddy;
				graphics.lineTo(x, y);
				x += gdx;
				y += gdy;
				--i;
			}
		}
		
		public static function randomPartition(n: uint, limit: uint = 0): Array
		{
			var result: Array = new Array
			
			while(n)
			{
				var l: int = (l > 0) ? limit : Math.max(n/4, 1)
				var m: uint = Math.min(Math.ceil(Math.random() * n), l)
				result.push(m)
				n -= m
			}
			
			return result
		}

		public static function stdRandom(): Number
		{
			var u1: Number = Math.random()
			var u2: Number = Math.random()
			
			// needs to be in (0,1]
			if(u1 == 0)
				u1 = 1
			if(u2 == 0)
				u2 = 1
			
			// Box–Muller transform
			return Math.sqrt(-2*Math.log(u1))*Math.cos(2*Math.PI*u2)
		}
		
		public static function isIn(a: Array, o: Object): int
		{
			for(var i: int = 0; i < a.length; ++i)
			{
				if(a[i].equals(o))
					return i
			}
			
			return -1
		}
		
		/**
		 * This method calculates the intersection (clipping) points of the two
		 * input rectangles with line segment defined by the centers of these two
		 * rectangles. The clipping points are saved in the input double array and
		 * whether or not the two rectangles overlap is returned.
		 */
		public static function getIntersection(rectA: Rectangle,
			rectB: Rectangle,
			result: Array): Boolean
		{
			//result[0-1] will contain clipPoint of rectA, result[2-3] will contain clipPoint of rectB
			
			var p1x: Number = rectA.x + (rectA.width/2);
			var p1y: Number = rectA.y + (rectA.height/2);		
			var p2x: Number = rectB.x + (rectB.width/2);
			var p2y: Number = rectB.y + (rectB.height/2);
			
			//if two rectangles intersect, then clipping points are centers
			if (rectA.intersects(rectB))
			{
				result[0] = p1x;
				result[1] = p1y;
				result[2] = p2x;
				result[3] = p2y;
				return true;
			}
			
			//variables for rectA
			var topLeftAx: Number = rectA.x;
			var topLeftAy: Number = rectA.y;
			var topRightAx: Number = rectA.right;
			var bottomLeftAx: Number = rectA.x;
			var bottomLeftAy: Number = rectA.bottom;
			var bottomRightAx: Number = rectA.right;
			var halfWidthA: Number = rectA.width/2;
			var halfHeightA: Number = rectA.height/2;
			
			//variables for rectB
			var topLeftBx: Number = rectB.x;
			var topLeftBy: Number = rectB.y;
			var topRightBx: Number = rectB.right;
			var bottomLeftBx: Number = rectB.x;
			var bottomLeftBy: Number = rectB.bottom;
			var bottomRightBx: Number = rectB.right;
			var halfWidthB: Number = rectB.width/2;
			var halfHeightB: Number = rectB.height/2;
			
			//flag whether clipping points are found
			var clipPointAFound: Boolean = false;
			var clipPointBFound: Boolean = false;
			
			// line is vertical
			if (p1x == p2x)
			{
				if(p1y > p2y)
				{
					result[0] = p1x;
					result[1] = topLeftAy;
					result[2] = p2x;
					result[3] = bottomLeftBy;
					return false;
				}
				else if(p1y < p2y)
				{
					result[0] = p1x;
					result[1] = bottomLeftAy;
					result[2] = p2x;
					result[3] = topLeftBy;
					return false;
				}
				else
				{
					//not line, return null;
				}
			}
			
			// line is horizontal
			else if (p1y == p2y)
			{
				if(p1x > p2x)
				{
					result[0] = topLeftAx;
					result[1] = p1y;
					result[2] = topRightBx;
					result[3] = p2y;
					return false;
				}
				else if(p1x < p2x)
				{
					result[0] = topRightAx;
					result[1] = p1y;
					result[2] = topLeftBx;
					result[3] = p2y;
					return false;
				}
				else
				{
					//not valid line, return null;
				}
			}
			else
			{
				//slopes of rectA's and rectB's diagonals
				var slopeA: Number = rectA.height / rectA.width;
				var slopeB: Number = rectB.height / rectB.width;
				
				//slope of line between center of rectA and center of rectB
				var slopePrime: Number = (p2y - p1y) / (p2x - p1x);
				var cardinalDirectionA: int;
				var cardinalDirectionB: int;
				var tempPointAx: Number;
				var tempPointAy: Number;
				var tempPointBx: Number;
				var tempPointBy: Number;
				
				//determine whether clipping point is the corner of nodeA
				if((-slopeA) == slopePrime)
				{
					if(p1x > p2x)
					{
						result[0] = bottomLeftAx;
						result[1] = bottomLeftAy;
						clipPointAFound = true;
					}
					else
					{
						result[0] = topRightAx;
						result[1] = topLeftAy;
						clipPointAFound = true;
					}
				}
				else if(slopeA == slopePrime)
				{
					if(p1x > p2x)
					{
						result[0] = topLeftAx;
						result[1] = topLeftAy;
						clipPointAFound = true;
					}
					else
					{
						result[0] = bottomRightAx;
						result[1] = bottomLeftAy;
						clipPointAFound = true;
					}
				}
				
				//determine whether clipping point is the corner of nodeB
				if((-slopeB) == slopePrime)
				{
					if(p2x > p1x)
					{
						result[2] = bottomLeftBx;
						result[3] = bottomLeftBy;
						clipPointBFound = true;
					}
					else
					{
						result[2] = topRightBx;
						result[3] = topLeftBy;
						clipPointBFound = true;
					}
				}
				else if(slopeB == slopePrime)
				{
					if(p2x > p1x)
					{
						result[2] = topLeftBx;
						result[3] = topLeftBy;
						clipPointBFound = true;
					}
					else
					{
						result[2] = bottomRightBx;
						result[3] = bottomLeftBy;
						clipPointBFound = true;
					}
				}
				
				//if both clipping points are corners
				if(clipPointAFound && clipPointBFound)
				{
					return false;
				}
				
				//determine Cardinal Direction of rectangles
				if(p1x > p2x)
				{
					if(p1y > p2y)
					{
						cardinalDirectionA = getCardinalDirection(slopeA, slopePrime, 4);
						cardinalDirectionB = getCardinalDirection(slopeB, slopePrime, 2);
					}
					else
					{
						cardinalDirectionA = getCardinalDirection(-slopeA, slopePrime, 3);
						cardinalDirectionB = getCardinalDirection(-slopeB, slopePrime, 1);
					}
				}
				else
				{
					if(p1y > p2y)
					{
						cardinalDirectionA = getCardinalDirection(-slopeA, slopePrime, 1);
						cardinalDirectionB = getCardinalDirection(-slopeB, slopePrime, 3);
					}
					else
					{
						cardinalDirectionA = getCardinalDirection(slopeA, slopePrime, 2);
						cardinalDirectionB = getCardinalDirection(slopeB, slopePrime, 4);
					}
				}
				//calculate clipping Point if it is not found before
				if(!clipPointAFound)
				{
					switch(cardinalDirectionA)
					{
						case 1:
							tempPointAy = topLeftAy;
							tempPointAx = p1x + ( -halfHeightA ) / slopePrime;
							result[0] = tempPointAx;
							result[1] = tempPointAy;
							break;
						case 2:
							tempPointAx = bottomRightAx;
							tempPointAy = p1y + halfWidthA * slopePrime;
							result[0] = tempPointAx;
							result[1] = tempPointAy;
							break;
						case 3:
							tempPointAy = bottomLeftAy;
							tempPointAx = p1x + halfHeightA / slopePrime;
							result[0] = tempPointAx;
							result[1] = tempPointAy;
							break;
						case 4:
							tempPointAx = bottomLeftAx;
							tempPointAy = p1y + ( -halfWidthA ) * slopePrime;
							result[0] = tempPointAx;
							result[1] = tempPointAy;
							break;
					}
				}
				if(!clipPointBFound)
				{
					switch(cardinalDirectionB)
					{
						case 1:
							tempPointBy = topLeftBy;
							tempPointBx = p2x + ( -halfHeightB ) / slopePrime;
							result[2] = tempPointBx;
							result[3] = tempPointBy;
							break;
						case 2:
							tempPointBx = bottomRightBx;
							tempPointBy = p2y + halfWidthB * slopePrime;
							result[2] = tempPointBx;
							result[3] = tempPointBy;
							break;
						case 3:
							tempPointBy = bottomLeftBy;
							tempPointBx = p2x + halfHeightB / slopePrime;
							result[2] = tempPointBx;
							result[3] = tempPointBy;
							break;
						case 4:
							tempPointBx = bottomLeftBx;
							tempPointBy = p2y + ( -halfWidthB ) * slopePrime;
							result[2] = tempPointBx;
							result[3] = tempPointBy;
							break;
					}
				}
				
			}
			
			return false;
		}
		
		/**
		 * This method returns in which cardinal direction does input point stays
		 * 1: North
		 * 2: East
		 * 3: South
		 * 4: West
		 */
		private static function getCardinalDirection(slope: Number,
			slopePrime: Number,
			line: int): int
		{
			if (slope > slopePrime)
			{
				return line;
			}
			else
			{
				return 1 + line % 4;
			}
		}
	}
}