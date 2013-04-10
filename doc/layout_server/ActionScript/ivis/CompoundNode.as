package ivis
{
	import flash.events.Event;
	import flash.display.DisplayObjectContainer;
	
	import mx.core.Container;
	
	public class CompoundNode extends Node
	{
		// child nodes
		private var _nodes: Array = new Array;
		
		// Constructor
		public function CompoundNode(id: String = null, x: Number = 0, y: Number = 0, cn: CompoundNode = null, data: Object = null)
		{
			super(id, x, y, cn, data);
		}
		
		//----------------------------------------
		//         Getter & Setter Methods
		//----------------------------------------
		public function get nodes(): Array
		{
			return _nodes
		}
		
		// this functions adds child node to this Compound node
		public function addNode(n: Node): void
		{
			this._nodes.push(n)
			n.parent = this
		}
		
		// this functions removes the given child node from this Compound node
		public function removeNode(n: Node): void
		{
			var i: int = _nodes.indexOf(n)
			
			this._nodes.splice(i, 1)[0];
			n.parent = null
		}
		
		override public function isCompound(): Boolean { return true }

		// this function finds the child node which has the given id
		public function nodeFromId(id: String): Node
		{
			for each(var n: Node in this._nodes)
			{
				if(n.id == id)
				{
					return n;
				}
				if (n.isCompound())
				{
					(n as CompoundNode).nodeFromId(id);
				}
			}
			
			return null;
		}
		
		// this function returns the XML equivalence of this compound node
		override public function asXML(): XML
		{
			var res: String = '<node id="' + this.id + '">' + 
				'<bounds height="' + this.height +
				'" width="' + this.width +
				'" x="' + this.x +
				'" y="' + this.y +
				'" />' +
				'<children>';
			
			for each (var n: Node in _nodes)
			{
				res += n.asXML();
			}
			
			res += '</children><clusterIDs>';
			
			for each (var c: uint in _clusterIDs)
			{
				res += '<clusterID>' + c + '</clusterID>';
			}
			
			res += '</clusterIDs></node>';
			
			return XML(res);
		}
		
	}
}