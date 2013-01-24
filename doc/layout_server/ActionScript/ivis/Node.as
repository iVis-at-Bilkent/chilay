package ivis
{	
	import mx.core.Container;
	import mx.core.UIComponent;
	
	import flash.geom.Rectangle;
	
	import ivis.Constants;
	
	public class Node
	{
		protected var _id: String = null;
		protected var _x: Number = 0;
		protected var _y: Number = 0;
		protected var _w: Number;
		protected var _h: Number;
		protected var _clusterIDs: Array = new Array;
		protected var _label: String;
		protected var _parent: CompoundNode = null;
		
		private static const ID_PATTERN: RegExp = /\d+/;
		private static var _idCount:uint = 0
		protected static const DEFAULT_WIDTH: uint = 40;
		protected static const DEFAULT_HEIGHT: uint = 40;
		
		// Constructor
		public function Node(id: String = null, x: Number = 0, y: Number = 0, cn: CompoundNode = null, data: Object = null)
		{
			if(id == null) 
			{
				this._id = _idCount++.toString();
			}			
			else 
			{
				this._id = id;
				
				if(ID_PATTERN.test(id)) {
					_idCount = Math.max(int(id) + 1, _idCount)
				}
			}
			
			this.x = x;
			this.y = y;
			this.width = DEFAULT_WIDTH;
			this.height = DEFAULT_HEIGHT;
			this.parent = cn;
		}
		
		//----------------------------------------
		//         Getter & Setter Methods
		//----------------------------------------
		public function get id(): String 
		{
			return this._id;
		}
		
		public function set id(value: String): void 
		{
			this._id = value;
		}
		
		public function get x(): Number
		{
			return this._x;
		}
		
		public function set x(value: Number): void 
		{
			this._x = value;
		}
		
		public function get y(): Number 
		{
			return this._y;
		}
		
		public function set y(value: Number): void 
		{
			this._y = value;
		}
		
		public function get width(): Number
		{
			return this._w;
		}
		
		public function set width(value: Number): void
		{
			this._w = value;
		}
		
		public function get height(): Number
		{
			return this._h;
		}
		
		public function set height(value: Number): void
		{
			this._h = value;
		}
		
		public function centerX() : Number
		{
			return (this.x + (this.width/2));
		}
		
		public function centerY() : Number
		{
			return (this.y + (this.height/2));
		}
		
		public function get label(): String 
		{
			return this._label;
		}
		
		public function set label(value: String): void 
		{
			this._label = value;
		}
		
		public function get parent(): CompoundNode
		{
			return this._parent;
		}	
		
		public function set parent(value: CompoundNode): void
		{
			this._parent = value;
		}	
		
		public function get clusterIDs(): Array
		{
			return this._clusterIDs;
		}
		
		public function set addClusterID(value: uint): void
		{
			this._clusterIDs.push(value);
		}
		
		public function isCompound(): Boolean { return false }
		
		public function bounds(includeGrapples: Boolean = false, exc: Node = null): *
		{
			return Utils.boundingRect([ Node(this) ])
		}
		
		public function toRectangle(): Rectangle
		{			
			return new Rectangle(this.x, this.y, this.width, this.height);
		}
		
		// this function returns the XML equivalence of this node
		public function asXML(): XML
		{
			var res: String = '<node id="' + this.id + '">' + 
				'<bounds height="' + this.height +
				'" width="' + this.width +
				'" x="' + this.x +
				'" y="' + this.y +
				'" />' + '<clusterIDs>';
			
			for each (var c: uint in _clusterIDs)
			{
				res += '<clusterID>' + c + '</clusterID>';
			}
			
			res += '</clusterIDs></node>';
			
			return XML(res);
		}
	}
}