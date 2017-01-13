package com.github.shadowsocks.plugin

import android.util.Log
import com.github.shadowsocks.utils.Commandline

import scala.collection.mutable

/**
  * @author Mygod
  */
class PluginConfiguration(val pluginsOptions: Map[String, PluginOptions], val selected: String) {
  private def this(plugins: Array[PluginOptions]) =
    this(plugins.map(opt => opt.id -> opt).toMap, if (plugins.isEmpty) "" else plugins(0).id)
  def this(plugin: String) = this(if (plugin == null) Array[PluginOptions]() else plugin.split("\n").map {
    case line if line.startsWith("kcptun ") =>
      val opt = new PluginOptions()
      opt.id = "kcptun"
      try {
        val args = mutable.Queue(Commandline.translateCommandline(line): _*)
        args.dequeue()
        while (args.nonEmpty) args.dequeue() match {
          case "--nocomp" => opt.put("nocomp", "1")
          case option if option.startsWith("--") => opt.put(option.substring(2), args.dequeue())
          case option => throw new IllegalArgumentException("Unknown kcptun parameter: " + option)
        }
      } catch {
        case exc: Exception => Log.w("PluginConfiguration", exc.getMessage)
      }
      opt
    case line => new PluginOptions(line)
  })

  def selectedOptions: PluginOptions = pluginsOptions(selected)

  override def toString: String = {
    val result = new mutable.ListBuffer[PluginOptions]()
    for ((id, opt) <- pluginsOptions) id match {
      case this.selected => result.prepend(opt)
      case _ => result.append(opt)
    }
    result.map(_.toString(false)).mkString("\n")
  }
}
